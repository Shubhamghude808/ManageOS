import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState, type FormEvent } from 'react'
import { Activity, CircleDollarSign, Clock3, Pause, Play, Square } from 'lucide-react'
import axios from 'axios'
import { clearApiCredentials, createPricingPlan, extendSession, getBranches, getPcs, getPricingPlans, getSessions, hasApiCredentials, sessionAction, setApiCredentials, startSession } from '../services/api'
import '../session-management.css'

function remainingLabel(seconds: number): string {
  const value = Math.max(0, seconds)
  return `${String(Math.floor(value / 3600)).padStart(2, '0')}:${String(Math.floor((value % 3600) / 60)).padStart(2, '0')}:${String(value % 60).padStart(2, '0')}`
}

export default function SessionManagement() {
  const client = useQueryClient()
  const [credentialsSet, setCredentialsSet] = useState(hasApiCredentials())
  const [username, setUsername] = useState('user')
  const [password, setPassword] = useState('')
  const [selectedBranch, setSelectedBranch] = useState('')
  const [pcId, setPcId] = useState('')
  const [planId, setPlanId] = useState('')
  const [planName, setPlanName] = useState('Standard')
  const [hourlyRate, setHourlyRate] = useState('100.00')
  const [planDuration, setPlanDuration] = useState('60')
  const [formError, setFormError] = useState('')
  const [now, setNow] = useState(Date.now())

  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(timer)
  }, [])

  const branches = useQuery({ queryKey: ['branches'], queryFn: getBranches, enabled: credentialsSet })
  const branchId = selectedBranch || branches.data?.[0]?.id || ''
  const pcs = useQuery({ queryKey: ['pcs', branchId], queryFn: () => getPcs(branchId), enabled: !!branchId, refetchInterval: 15_000 })
  const plans = useQuery({ queryKey: ['pricing-plans', branchId], queryFn: () => getPricingPlans(branchId), enabled: !!branchId })
  const activePlans = plans.data?.filter(plan => plan.active) ?? []
  const sessions = useQuery({ queryKey: ['sessions', branchId], queryFn: () => getSessions(branchId), enabled: !!branchId, refetchInterval: 5_000 })
  const refreshData = async () => {
    await Promise.all([
      client.invalidateQueries({ queryKey: ['sessions', branchId] }),
      client.invalidateQueries({ queryKey: ['pcs', branchId] }),
    ])
  }
  const createPlan = useMutation({ mutationFn: createPricingPlan, onSuccess: async () => { setFormError(''); await client.invalidateQueries({ queryKey: ['pricing-plans', branchId] }) }, onError: () => setFormError('Could not save the plan. Check the values and backend connection.') })
  const beginSession = useMutation({ mutationFn: startSession, onSuccess: async () => { setFormError(''); await refreshData() }, onError: error => { const apiMessage = axios.isAxiosError<{ error?: { message?: string } }>(error) ? error.response?.data?.error?.message : undefined; setFormError(apiMessage || 'Could not start the session. Confirm the PC is available and the rate belongs to this branch.') } })
  const command = useMutation({ mutationFn: ({ id, action }: { id: string; action: 'pause' | 'resume' | 'end' }) => sessionAction(id, action), onSuccess: async () => { setFormError(''); await refreshData() }, onError: () => setFormError('Session command failed. Refresh the session list and try again.') })
  const extend = useMutation({ mutationFn: (id: string) => extendSession(id, 1800), onSuccess: async () => { setFormError(''); await refreshData() }, onError: () => setFormError('Could not extend this session.') })

  function connect(event: FormEvent) {
    event.preventDefault()
    setApiCredentials(username, password)
    setCredentialsSet(true)
    void client.invalidateQueries({ queryKey: ['branches'] })
  }
  function savePlan(event: FormEvent) {
    event.preventDefault()
    if (branchId) createPlan.mutate({ branchId, name: planName.trim(), hourlyRate: Number(hourlyRate), durationMinutes: Number(planDuration), currency: 'INR' })
  }
  function begin(event: FormEvent) {
    event.preventDefault()
    setFormError('')
    const selectedPcId = pcId || pcs.data?.find(pc => pc.status === 'AVAILABLE')?.id
    const selectedPlan = activePlans.find(plan => plan.id === planId)
    const selectedDurationMinutes = selectedPlan ? Number(selectedPlan.durationMinutes) || 60 : 0
    if (selectedPcId && selectedPlan && selectedDurationMinutes > 0) {
      beginSession.mutate({ pcId: selectedPcId, pricingPlanId: selectedPlan.id, allocatedSeconds: selectedDurationMinutes * 60 })
    } else {
      setFormError('Select an available PC and plan before starting the session.')
    }
  }

  return <>
    <div className="page-heading"><div><div className="eyebrow">LIVE OPERATIONS</div><h1>Gaming sessions</h1><p>Session time and charges are calculated by the local server.</p></div></div>
    {!credentialsSet && <section className="panel setup-panel"><h2>Connect to your local backend</h2><p>Use the staff username and password from your local `.env`.</p><form className="inline-form" onSubmit={connect}><label>Username<input value={username} onChange={e => setUsername(e.target.value)} required /></label><label>Password<input type="password" value={password} onChange={e => setPassword(e.target.value)} required /></label><button className="primary-button">Connect</button></form></section>}
    {credentialsSet && branches.isError && <section className="panel setup-panel"><h2>Could not load branches</h2><p>Check that the local backend is running and credentials match `.env`.</p><button className="secondary-button" onClick={() => { clearApiCredentials(); setCredentialsSet(false) }}>Change credentials</button></section>}
    {credentialsSet && branches.isLoading && <section className="panel setup-panel">Loading cafe branches…</section>}
    {credentialsSet && branches.isSuccess && branches.data.length === 0 && <section className="panel setup-panel"><h2>Create a branch first</h2><p>Open Gaming PCs to set up your cafe branch before configuring sessions.</p></section>}
    {branchId && <>
        <section className="panel setup-panel"><div className="session-toolbar"><div><h2>Branch and hourly rate</h2><p>Billing uses the rate snapshot saved when a session starts.</p></div><select aria-label="Branch" value={branchId} onChange={e => { setSelectedBranch(e.target.value); setPcId(''); setPlanId('') }}>{branches.data?.map(branch => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select></div>
        {plans.isLoading ? <p className="inline-note">Loading plans…</p> : <form className="inline-form" onSubmit={savePlan}><label>Plan name<input value={planName} onChange={e => setPlanName(e.target.value)} required maxLength={120}/></label><label>Rate (INR/hour)<input type="number" min="0.01" step="0.01" value={hourlyRate} onChange={e => setHourlyRate(e.target.value)} required/></label><label>Duration (minutes)<input type="number" min="1" max="1440" value={planDuration} onChange={e => setPlanDuration(e.target.value)} required/></label><button className="primary-button" disabled={createPlan.isPending}>Add plan</button></form>}
      </section>
      {activePlans.length > 0 && <section className="panel setup-panel"><h2>Start a session</h2><form className="inline-form start-session-form" onSubmit={begin}><label>Available PC<select value={pcId || pcs.data?.find(pc => pc.status === 'AVAILABLE')?.id || ''} onChange={e => setPcId(e.target.value)} required><option value="" disabled>Select a PC</option>{pcs.data?.filter(pc => pc.status === 'AVAILABLE').map(pc => <option key={pc.id} value={pc.id}>{pc.name}</option>)}</select></label><label>Plan / hourly rate<select value={planId} onChange={e => setPlanId(e.target.value)} required><option value="" disabled>Select a plan</option>{activePlans.map(plan => <option key={plan.id} value={plan.id}>{plan.name} · {plan.currency} {Number(plan.hourlyRate).toFixed(2)}/hour · {Number(plan.durationMinutes) || 60} min</option>)}</select></label><button className="primary-button" disabled={beginSession.isPending || !pcs.data?.some(pc => pc.status === 'AVAILABLE') || !planId}><Play size={14}/> Start session</button></form>{!pcs.data?.some(pc => pc.status === 'AVAILABLE') && <p className="inline-note">No PCs are currently available.</p>}</section>}
      {formError && <div className="form-error session-error">{formError}</div>}
      <section className="panel sessions-panel"><header><div><h2>Session history</h2><p>Active timers refresh from the backend every five seconds.</p></div><span className="local-badge"><i/> Local-first</span></header>
        {sessions.isLoading ? <div className="empty-state">Loading sessions…</div> : sessions.isError ? <div className="empty-state">Could not load sessions.</div> : sessions.data?.length ? <div className="session-list">{sessions.data.map(session => {
          const elapsedSinceRefresh = session.status === 'ACTIVE' ? Math.max(0, Math.floor((now - sessions.dataUpdatedAt) / 1000)) : 0
          const remaining = Math.max(0, session.remainingSeconds - elapsedSinceRefresh)
          return <article className="session-card" key={session.id}><div className="session-card-top"><div className="session-pc-icon"><Activity size={18}/></div><div className="session-title"><strong>{session.pcName}</strong><span>{session.pricingPlanName} · {session.currency} {Number(session.hourlyRate).toFixed(2)}/hour</span></div><span className={`status-pill ${session.status.toLowerCase()}`}>{session.status.toLowerCase()}</span></div>
            <div className="session-metrics"><div><span>Time remaining</span><strong className={remaining < 300 && session.status === 'ACTIVE' ? 'time-low' : ''}>{session.status === 'ACTIVE' || session.status === 'PAUSED' ? remainingLabel(remaining) : '—'}</strong></div><div><span>Allocated</span><strong>{Math.floor(session.allocatedSeconds / 60)} min</strong></div><div><span>{session.status === 'COMPLETED' ? 'Amount due' : 'Started'}</span><strong>{session.status === 'COMPLETED' ? `${session.currency} ${Number(session.amountDue ?? 0).toFixed(2)}` : new Date(session.startedAt).toLocaleTimeString()}</strong></div></div>
            {(session.status === 'ACTIVE' || session.status === 'PAUSED') && <div className="session-actions">{session.status === 'ACTIVE' ? <button className="secondary-button" onClick={() => command.mutate({ id: session.id, action: 'pause' })}><Pause size={13}/> Pause</button> : <button className="secondary-button" onClick={() => command.mutate({ id: session.id, action: 'resume' })}><Play size={13}/> Resume</button>}<button className="secondary-button" onClick={() => extend.mutate(session.id)}><Clock3 size={13}/> +30 min</button><button className="end-button" onClick={() => { if (window.confirm(`End ${session.pcName} session and calculate the amount due?`)) command.mutate({ id: session.id, action: 'end' }) }}><Square size={12}/> End</button></div>}
          </article>
        })}</div> : <div className="empty-state"><div className="empty-icon"><CircleDollarSign size={20}/></div><strong>No sessions yet</strong><span>Select an available PC and start the first session.</span></div>}
      </section>
    </>}
  </>
}
