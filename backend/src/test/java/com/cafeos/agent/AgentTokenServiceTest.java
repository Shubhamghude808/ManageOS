package com.cafeos.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import com.cafeos.branch.Branch;
import com.cafeos.branch.BranchRepository;
import com.cafeos.pc.GamingPc;
import com.cafeos.pc.GamingPcRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AgentTokenServiceTest {
    private final GamingPcRepository pcs = mock(GamingPcRepository.class);
    private final BranchRepository branches = mock(BranchRepository.class);
    private final AgentTokenService service = new AgentTokenService(pcs, branches);

    @Test
    void issuesRandomTokenButStoresOnlyItsHash() {
        UUID pcId = UUID.randomUUID();
        GamingPc pc = pc(pcId);
        when(pcs.findById(pcId)).thenReturn(Optional.of(pc));

        AgentTokenService.IssuedAgentToken issued = service.issue(pcId);

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(pc).setAgentCredentialHash(hashCaptor.capture());
        String storedHash = hashCaptor.getValue();
        assertEquals(pcId, issued.pcId());
        assertNotEquals(issued.token(), storedHash);
        assertEquals(64, storedHash.length());
        assertTrue(issued.token().matches("[A-Za-z0-9_-]{43}"));

        when(pcs.findByAgentCredentialHashAndDeletedFalse(storedHash)).thenReturn(Optional.of(pc));
        assertEquals(Optional.of(pcId), service.authenticate(issued.token()));
        assertFalse(service.authenticate("invalid-token").isPresent());
    }

    @Test
    void issuingAgainRotatesTheCredential() {
        UUID pcId = UUID.randomUUID();
        GamingPc pc = pc(pcId);
        when(pcs.findById(pcId)).thenReturn(Optional.of(pc));

        AgentTokenService.IssuedAgentToken first = service.issue(pcId);
        AgentTokenService.IssuedAgentToken second = service.issue(pcId);
        assertNotEquals(first.token(), second.token());

        ArgumentCaptor<String> hashes = ArgumentCaptor.forClass(String.class);
        verify(pc, org.mockito.Mockito.times(2)).setAgentCredentialHash(hashes.capture());
        String firstHash = hashes.getAllValues().get(0);
        String secondHash = hashes.getAllValues().get(1);
        when(pcs.findByAgentCredentialHashAndDeletedFalse(firstHash)).thenReturn(Optional.empty());
        when(pcs.findByAgentCredentialHashAndDeletedFalse(secondHash)).thenReturn(Optional.of(pc));

        assertTrue(service.authenticate(first.token()).isEmpty());
        assertEquals(Optional.of(pcId), service.authenticate(second.token()));
    }

    @Test
    void createsPcInTheOnlyBranchWhenNoneMatches() {
        UUID pcId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        String machineId = "machine-123";
        Branch branch = mock(Branch.class);
        GamingPc saved = pc(pcId);
        when(branch.getId()).thenReturn(branchId);
        when(branches.findAllByOrderByNameAsc()).thenReturn(java.util.List.of(branch));
        when(pcs.existsByBranchIdAndNameIgnoreCase(branchId, "HOST-01")).thenReturn(false);
        when(pcs.save(any(GamingPc.class))).thenReturn(saved);
        when(pcs.findById(pcId)).thenReturn(Optional.of(saved));
        when(pcs.findByAgentMachineIdAndDeletedFalse(machineId)).thenReturn(Optional.empty());
        when(pcs.findByHostnameIgnoreCaseAndDeletedFalse("HOST-01")).thenReturn(Optional.empty());

        AgentTokenService.IssuedAgentToken issued = service.issueForEnrollment(machineId, "HOST-01", null);

        assertEquals(pcId, issued.pcId());
        verify(pcs).save(any(GamingPc.class));
        verify(saved).setAgentMachineId(machineId);
    }

    private static GamingPc pc(UUID pcId) {
        GamingPc pc = mock(GamingPc.class);
        when(pc.getId()).thenReturn(pcId);
        return pc;
    }
}
