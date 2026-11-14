import axios from 'axios'

/**
 * Dedicated client for the PC agent endpoints.
 *
 * The backend distinguishes two credential kinds:
 *   - Staff HTTP Basic (username/password) for /branches, /pcs, /sessions, ...
 *   - Agent bearer token for /api/agent/** (issued once per PC on the Gaming PCs page)
 *
 * This client carries the agent bearer token only. It never touches the staff
 * credentials, so the player view cannot interfere with admin operations.
 */
export const agentApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  timeout: 5000,
})

export function setAgentToken(token: string | null): void {
  if (token) {
    agentApi.defaults.headers.common.Authorization = `Bearer ${token}`
  } else {
    delete agentApi.defaults.headers.common.Authorization
  }
}

export function clearAgentToken(): void {
  delete agentApi.defaults.headers.common.Authorization
}

export interface AgentSession {
  id: string
  pcId: string
  pcName: string
  status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'
  pricingPlanId: string
  pricingPlanName: string
  hourlyRate: number
  currency: string
  allocatedSeconds: number
  remainingSeconds: number
  startedAt: string
  pausedAt: string | null
  accumulatedPausedSeconds: number
  expiresAt: string | null
  endedAt: string | null
  amountDue: number | null
}

/**
 * Returns the authenticated PC's current open session, or null when the PC has
 * no active session. The server is authoritative for remainingSeconds; the HUD
 * only interpolates the countdown between refreshes.
 */
export async function getAgentSession(): Promise<AgentSession | null> {
  const response = await agentApi.get<{ success: boolean; data: AgentSession | null }>(
    '/agent/session',
  )
  return response.data.data
}

export async function getAgentHeartbeat(): Promise<{
  agentStatus: string
  agentVersion: string
  cpuPercent: number
  memoryPercent: number
  lastSeenAt: string | null
} | null> {
  const response = await agentApi.get<{
    success: boolean
    data: {
      agentStatus: string
      agentVersion: string
      cpuPercent: number
      memoryPercent: number
      lastSeenAt: string | null
    } | null
  }>('/agent/heartbeat')
  return response.data.data
}