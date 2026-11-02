import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  timeout: 5000,
})

export type ApiEnvelope<T> = { success: boolean; data: T; timestamp: string }
export type Health = { status: string; time: string }
export type Branch = { id: string; cafeName: string; name: string; timezone: string }
export type GamingPc = { id: string; branchId: string; branchName: string; name: string; status: 'AVAILABLE' | 'OCCUPIED' | 'OFFLINE' | 'MAINTENANCE'; hostname: string | null; createdAt: string; lastHeartbeatAt: string | null; agentTokenIssued: boolean }
export type AgentToken = { pcId: string; token: string }
export type PricingPlan = { id: string; branchId: string; name: string; hourlyRate: number; durationMinutes: number; currency: string; active: boolean }
export type GamingSession = {
  id: string; branchId: string; pcId: string; pcName: string; pricingPlanId: string; pricingPlanName: string
  status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'; hourlyRate: number; currency: string
  allocatedSeconds: number; remainingSeconds: number; startedAt: string; pausedAt: string | null
  accumulatedPausedSeconds: number; expiresAt: string | null; endedAt: string | null; amountDue: number | null
}

let configuredCredentials = false
export function hasApiCredentials(): boolean { return configuredCredentials }
export function setApiCredentials(username: string, password: string): void {
  api.defaults.auth = { username, password }
  configuredCredentials = true
}
export function clearApiCredentials(): void {
  delete api.defaults.auth
  configuredCredentials = false
}

export async function getHealth(): Promise<Health> {
  const response = await api.get<ApiEnvelope<Health>>('/health')
  return response.data.data
}
export async function getBranches(): Promise<Branch[]> {
  const response = await api.get<ApiEnvelope<Branch[]>>('/branches')
  return response.data.data
}
export async function createBranch(input: { cafeName: string; name: string; timezone: string }): Promise<Branch> {
  const response = await api.post<ApiEnvelope<Branch>>('/branches', input)
  return response.data.data
}
export async function getPcs(branchId: string): Promise<GamingPc[]> {
  const response = await api.get<ApiEnvelope<GamingPc[]>>('/pcs', { params: { branchId } })
  return response.data.data
}
export async function createPc(input: { branchId: string; name: string; hostname?: string }): Promise<GamingPc> {
  const response = await api.post<ApiEnvelope<GamingPc>>('/pcs', input)
  return response.data.data
}
export async function updatePc(id: string, input: { branchId: string; name: string; hostname?: string; status: GamingPc['status'] }): Promise<GamingPc> {
  const response = await api.put<ApiEnvelope<GamingPc>>(`/pcs/${id}`, input)
  return response.data.data
}
export async function issueAgentToken(pcId: string): Promise<AgentToken> {
  const response = await api.post<ApiEnvelope<AgentToken>>(`/pcs/${pcId}/agent-token`)
  return response.data.data
}
export async function getPricingPlans(branchId: string): Promise<PricingPlan[]> {
  const response = await api.get<ApiEnvelope<PricingPlan[]>>('/pricing-plans', { params: { branchId } })
  return response.data.data
}
export async function createPricingPlan(input: { branchId: string; name: string; hourlyRate: number; durationMinutes: number; currency: string }): Promise<PricingPlan> {
  const response = await api.post<ApiEnvelope<PricingPlan>>('/pricing-plans', input)
  return response.data.data
}
export async function getSessions(branchId: string): Promise<GamingSession[]> {
  const response = await api.get<ApiEnvelope<GamingSession[]>>('/sessions', { params: { branchId } })
  return response.data.data
}
export async function startSession(input: { pcId: string; pricingPlanId: string; allocatedSeconds: number }): Promise<GamingSession> {
  const response = await api.post<ApiEnvelope<GamingSession>>('/sessions', input)
  return response.data.data
}
export async function sessionAction(id: string, action: 'pause' | 'resume' | 'end'): Promise<GamingSession> {
  const response = await api.post<ApiEnvelope<GamingSession>>(`/sessions/${id}/${action}`)
  return response.data.data
}
export async function extendSession(id: string, addedSeconds: number): Promise<GamingSession> {
  const response = await api.post<ApiEnvelope<GamingSession>>(`/sessions/${id}/extend`, { addedSeconds })
  return response.data.data
}
