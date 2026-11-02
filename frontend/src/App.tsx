import { NavLink, Routes, Route, Link } from 'react-router-dom'
import { Activity, BarChart3, CircleDollarSign, Computer, LayoutDashboard, Settings, Users, Wifi, WifiOff, Pencil, RefreshCw, KeyRound, Copy, Check } from 'lucide-react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clearApiCredentials, createBranch, createPc, getBranches, getHealth, getPcs, getSessions, hasApiCredentials, issueAgentToken, setApiCredentials, updatePc, type GamingPc } from './services/api'
import { useState, type FormEvent } from 'react'
import './pc-management.css'
import SessionManagement from './features/SessionManagement'

const nav = [
  { label: 'Overview', path: '/', icon: LayoutDashboard },
  { label: 'Gaming PCs', path: '/pcs', icon: Computer },
  { label: 'Sessions', path: '/sessions', icon: Activity },
  { label: 'Customers', path: '/customers', icon: Users },
  { label: 'Revenue', path: '/revenue', icon: CircleDollarSign },
  { label: 'Reports', path: '/reports', icon: BarChart3 },
]

function Dashboard() {
  const health = useQuery({ queryKey: ['backend-health'], queryFn: getHealth, retry: 1, refetchInterval: 30_000 })
  const branches = useQuery({ queryKey: ['branches'], queryFn: getBranches, enabled: hasApiCredentials() })
  const branchId = branches.data?.[0]?.id
  const pcs = useQuery({ queryKey: ['pcs', branchId], queryFn: () => getPcs(branchId!), enabled: !!branchId, refetchInterval: 15_000 })
  const sessions = useQuery({ queryKey: ['sessions', branchId], queryFn: () => getSessions(branchId!), enabled: !!branchId, refetchInterval: 5_000 })
  const activeSessions = sessions.data?.filter(session => session.status === 'ACTIVE' || session.status === 'PAUSED').length
  const recentSessions = sessions.data?.slice(0, 5) ?? []
  const activityLabel = (status: string) => status === 'ACTIVE' ? 'Session started' : status === 'PAUSED' ? 'Session paused' : status === 'COMPLETED' ? 'Session completed' : 'Session cancelled'
  return <>
    <div className="page-heading"><div><div className="eyebrow">MONDAY, NOVEMBER 02, 2026</div><h1>Good evening, Admin</h1><p>Here’s what’s happening at your cafe today.</p></div><Link className="primary-button" to="/sessions">＋ Start a session</Link></div>
    <section className="stats-grid">
      <article className="stat-card"><span>Registered PCs</span><strong>{pcs.data ? pcs.data.length : '—'}</strong><em>{pcs.data ? 'In ' + (branches.data?.[0]?.name ?? 'your branch') : 'Open PC management to load inventory'}</em></article>
      <article className="stat-card"><span>Active sessions</span><strong>{activeSessions ?? '—'}</strong><em>{activeSessions === undefined ? 'Open sessions to load live activity' : 'Active or paused on this branch'}</em></article>
      <article className="stat-card"><span>Today’s revenue</span><strong>₹—</strong><em>Payments will appear here</em></article>
      <article className="stat-card"><span>Customers today</span><strong>—</strong><em>Customer tracking is not configured</em></article>
    </section>
    <section className="content-grid">
      <article className="panel"><header><div><h2>Gaming PCs</h2><p>Registered devices in your branch</p></div><Link className="text-button" to="/pcs">Manage PCs →</Link></header>{pcs.data?.length ? <div className="pc-summary-list">{pcs.data.slice(0, 4).map(pc => <div className="pc-summary-row" key={pc.id}><Computer size={16}/><strong>{pc.name}</strong><span className={`status-pill ${pc.status.toLowerCase()}`}>{pc.status.toLowerCase()}</span></div>)}{pcs.data.length > 4 && <Link className="summary-more" to="/pcs">View all {pcs.data.length} PCs →</Link>}</div> : <div className="empty-state"><div className="empty-icon"><Computer size={22}/></div><strong>{pcs.isError ? 'Connect to view PCs' : 'PC management'}</strong><span>Register gaming PCs, set maintenance status and review availability.</span><Link className="secondary-button" to="/pcs">Open PC management</Link></div>}</article>
      <article className="panel"><header><div><h2>Recent activity</h2><p>Latest session events from your cafe</p></div><Link className="text-button" to="/sessions">View sessions →</Link></header>{recentSessions.length ? <div className="activity-list">{recentSessions.map(session => <div className="activity-row" key={session.id}><div className={`activity-icon ${session.status.toLowerCase()}`}><Activity size={15}/></div><div className="activity-copy"><strong>{activityLabel(session.status)} · {session.pcName}</strong><span>{new Date(session.endedAt ?? session.startedAt).toLocaleString()} · {session.status === 'COMPLETED' ? `${session.currency} ${Number(session.amountDue ?? 0).toFixed(2)} due` : `${Math.floor(session.allocatedSeconds / 60)} min allocated`}</span></div><span className={`status-pill ${session.status.toLowerCase()}`}>{session.status.toLowerCase()}</span></div>)}</div> : <div className="empty-state"><div className="empty-icon"><Activity size={22}/></div><strong>{sessions.isError ? 'Could not load activity' : hasApiCredentials() ? 'Quiet for now' : 'Connect to see activity'}</strong><span>{sessions.isError ? 'Check your backend connection and staff credentials.' : 'Started, paused, and completed sessions will appear here.'}</span>{!hasApiCredentials() && <Link className="secondary-button" to="/sessions">Open sessions</Link>}</div>}</article>
    </section>
    <div className={`connection ${health.isSuccess ? 'connected' : ''}`}>
      {health.isSuccess ? <Wifi size={15}/> : <WifiOff size={15}/>}
      <span>{health.isSuccess ? 'Local server connected' : health.isPending ? 'Checking local server…' : 'Local server not connected'}</span>
      {health.isError && <button onClick={() => void health.refetch()}>Retry</button>}
    </div>
  </>
}

function Placeholder({ title }: { title: string }) {
  return <div className="page-heading"><div><div className="eyebrow">CAFE MANAGEMENT</div><h1>{title}</h1><p>This section will be available as the foundation modules are implemented.</p></div></div>
}

function PcManagement() {
  const client = useQueryClient()
  const [credentialsSet, setCredentialsSet] = useState(hasApiCredentials())
  const [username, setUsername] = useState('user')
  const [password, setPassword] = useState('')
  const [cafeName, setCafeName] = useState('')
  const [branchName, setBranchName] = useState('Main Branch')
  const [pcName, setPcName] = useState('')
  const [hostname, setHostname] = useState('')
  const [selectedBranch, setSelectedBranch] = useState('')
  const [formError, setFormError] = useState('')
  const [agentTokens, setAgentTokens] = useState<Record<string, string>>({})
  const [copiedTokenPc, setCopiedTokenPc] = useState('')
  const branches = useQuery({ queryKey: ['branches'], queryFn: getBranches, enabled: credentialsSet })
  const branchId = selectedBranch || branches.data?.[0]?.id || ''
  const pcs = useQuery({ queryKey: ['pcs', branchId], queryFn: () => getPcs(branchId), enabled: credentialsSet && !!branchId, refetchInterval: 15_000 })
  const createBranchMutation = useMutation({ mutationFn: createBranch, onSuccess: async (branch) => { setSelectedBranch(branch.id); await client.invalidateQueries({ queryKey: ['branches'] }); setFormError('') }, onError: () => setFormError('Could not create branch. Check the API credentials and branch details.') })
  const createPcMutation = useMutation({ mutationFn: createPc, onSuccess: async () => { setPcName(''); setHostname(''); setFormError(''); await client.invalidateQueries({ queryKey: ['pcs', branchId] }) }, onError: () => setFormError('Could not add this PC. A PC with the same name may already exist.') })
  const updatePcMutation = useMutation({ mutationFn: ({ id, input }: { id: string; input: { branchId: string; name: string; hostname?: string; status: GamingPc['status'] } }) => updatePc(id, input), onSuccess: async () => { await client.invalidateQueries({ queryKey: ['pcs', branchId] }) }, onError: () => setFormError('Could not update this PC.') })
  const issueTokenMutation = useMutation({ mutationFn: issueAgentToken, onSuccess: async ({ pcId, token }) => { setAgentTokens(current => ({ ...current, [pcId]: token })); setCopiedTokenPc(''); await client.invalidateQueries({ queryKey: ['pcs', branchId] }) }, onError: () => setFormError('Could not issue an agent token.') })

  function connect(event: FormEvent) {
    event.preventDefault()
    setApiCredentials(username, password)
    setCredentialsSet(true)
    void client.invalidateQueries({ queryKey: ['branches'] })
  }
  function addBranch(event: FormEvent) {
    event.preventDefault()
    if (cafeName.trim() && branchName.trim()) createBranchMutation.mutate({ cafeName: cafeName.trim(), name: branchName.trim(), timezone: 'Asia/Kolkata' })
  }
  function addPc(event: FormEvent) {
    event.preventDefault()
    if (branchId && pcName.trim()) createPcMutation.mutate({ branchId, name: pcName.trim(), hostname: hostname.trim() || undefined })
  }
  function editPc(pc: GamingPc) {
    const name = window.prompt('PC name', pc.name)
    if (!name?.trim()) return
    const status = window.prompt('Status: AVAILABLE, OFFLINE, or MAINTENANCE', pc.status === 'OCCUPIED' ? 'AVAILABLE' : pc.status)?.toUpperCase()
    if (!status || !['AVAILABLE', 'OFFLINE', 'MAINTENANCE'].includes(status)) return
    updatePcMutation.mutate({ id: pc.id, input: { branchId, name: name.trim(), hostname: pc.hostname ?? undefined, status: status as GamingPc['status'] } })
  }

  return <>
    <div className="page-heading"><div><div className="eyebrow">DEVICE MANAGEMENT</div><h1>Gaming PCs</h1><p>Register and manage the PCs in your cafe.</p></div></div>
    {!credentialsSet ? <section className="panel setup-panel"><h2>Connect to your local backend</h2><p>Use the Spring Security username and password shown in the backend startup console.</p><form className="inline-form" onSubmit={connect}><label>Username<input value={username} onChange={e => setUsername(e.target.value)} autoComplete="username" required /></label><label>Password<input type="password" value={password} onChange={e => setPassword(e.target.value)} autoComplete="current-password" required /></label><button className="primary-button">Connect</button></form></section> : null}
    {credentialsSet && branches.isError ? <section className="panel setup-panel"><h2>Could not access branches</h2><p>Check that the backend is running and that these credentials match the backend startup console.</p><button className="secondary-button" onClick={() => { clearApiCredentials(); setCredentialsSet(false) }}>Change credentials</button></section> : null}
    {credentialsSet && branches.isLoading ? <section className="panel setup-panel">Loading branches…</section> : null}
    {credentialsSet && branches.isSuccess && branches.data.length === 0 ? <section className="panel setup-panel"><h2>Set up your cafe branch</h2><p>Create the first cafe and branch before adding gaming PCs.</p><form className="inline-form" onSubmit={addBranch}><label>Cafe name<input value={cafeName} onChange={e => setCafeName(e.target.value)} required maxLength={160}/></label><label>Branch name<input value={branchName} onChange={e => setBranchName(e.target.value)} required maxLength={160}/></label><button className="primary-button" disabled={createBranchMutation.isPending}>Create branch</button></form></section> : null}
    {credentialsSet && branches.isSuccess && branches.data.length > 0 ? <>
      <section className="panel setup-panel"><div className="pc-toolbar"><div><h2>Branch inventory</h2><p>PC status is managed by CafeOS session and agent activity.</p></div><select aria-label="Branch" value={branchId} onChange={e => setSelectedBranch(e.target.value)}>{branches.data.map(branch => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select></div>
        <form className="inline-form add-pc-form" onSubmit={addPc}><label>PC name<input placeholder="Gaming PC 01" value={pcName} onChange={e => setPcName(e.target.value)} required maxLength={100}/></label><label>Windows hostname<input placeholder="PC-01 (optional)" value={hostname} onChange={e => setHostname(e.target.value)} maxLength={255}/></label><button className="primary-button" disabled={createPcMutation.isPending}>＋ Add PC</button></form>
        {formError && <div className="form-error">{formError}</div>}
      </section>
      <section className="panel pc-list-panel"><header><div><h2>Registered PCs</h2><p>{pcs.data?.length ?? 0} devices in {branches.data.find(b => b.id === branchId)?.name}</p></div><button className="text-button" onClick={() => void pcs.refetch()}><RefreshCw size={13}/> Refresh</button></header>
        {pcs.isLoading ? <div className="empty-state">Loading PCs…</div> : pcs.isError ? <div className="empty-state">Could not load PCs. Check your connection and backend credentials.</div> : pcs.data?.length ? <div className="pc-table">{pcs.data.map(pc => { const config = `CAFEOS_SERVER_URL=http://<ADMIN-PC-LAN-IP>:8080\nCAFEOS_PC_ID=${pc.id}\nCAFEOS_AGENT_TOKEN=${agentTokens[pc.id]}`; return <div key={pc.id}><div className="pc-row"><div className="pc-icon"><Computer size={17}/></div><div className="pc-details"><strong>{pc.name}</strong><span>{pc.hostname || 'Hostname not registered'} · {pc.lastHeartbeatAt ? `Heartbeat ${new Date(pc.lastHeartbeatAt).toLocaleTimeString()}` : 'Agent not connected'}</span></div><span className={`status-pill ${pc.status.toLowerCase()}`}>{pc.status.toLowerCase()}</span><button className="icon-button" aria-label={`${pc.agentTokenIssued ? 'Rotate' : 'Issue'} agent token for ${pc.name}`} title={pc.agentTokenIssued ? 'Rotate agent token' : 'Issue agent token'} onClick={() => { if (!pc.agentTokenIssued || window.confirm('Rotate this PC token? Any existing agent token will stop working.')) issueTokenMutation.mutate(pc.id) }}><KeyRound size={15}/></button><button className="icon-button" aria-label={`Edit ${pc.name}`} onClick={() => editPc(pc)}><Pencil size={15}/></button></div>{agentTokens[pc.id] && <div className="token-panel"><div><strong>One-time agent configuration</strong><span>Replace &lt;ADMIN-PC-LAN-IP&gt; with the address of this CafeOS computer. Store the token securely on {pc.name}; rotating it revokes the previous token.</span><pre>{config}</pre></div><button className="secondary-button" onClick={() => void navigator.clipboard.writeText(config).then(() => setCopiedTokenPc(pc.id))}>{copiedTokenPc === pc.id ? <Check size={14}/> : <Copy size={14}/>} {copiedTokenPc === pc.id ? 'Copied' : 'Copy config'}</button></div>}</div> })}</div> : <div className="empty-state"><div className="empty-icon"><Computer size={22}/></div><strong>No PCs registered yet</strong><span>Add the first PC using the form above.</span></div>}
      </section>
    </> : null}
  </>
}

export default function App() {
  return <div className="app-shell">
    <aside className="sidebar"><div className="brand"><div className="brand-mark">C</div><div><strong>CafeOS</strong><span>CONTROL ROOM</span></div></div>
      <div className="branch-switch"><span className="branch-dot"/> Main Branch <span className="chevron">⌄</span></div>
      <div className="nav-label">WORKSPACE</div><nav>{nav.map(({ label, path, icon: Icon }) => <NavLink key={path} to={path} end={path === '/'} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}><Icon size={18}/><span>{label}</span>{label === 'Gaming PCs' && <b>—</b>}</NavLink>)}</nav>
      <div className="sidebar-bottom"><NavLink to="/settings" className="nav-link"><Settings size={18}/><span>Settings</span></NavLink><div className="profile"><div className="avatar">A</div><div><strong>Admin User</strong><span>Owner</span></div><span className="chevron">···</span></div></div>
    </aside>
    <main className="main-area"><header className="topbar"><span className="breadcrumb">CafeOS <span>/</span> Overview</span><div className="top-actions"><span className="local-badge"><i/> Local mode</span><button className="icon-button" aria-label="Notifications">♧</button><div className="avatar small">A</div></div></header><div className="page-content"><Routes><Route path="/" element={<Dashboard/>}/><Route path="/pcs" element={<PcManagement/>}/><Route path="/sessions" element={<SessionManagement/>}/>{nav.filter(({ path }) => path !== '/' && path !== '/pcs' && path !== '/sessions').map(({ label, path }) => <Route key={path} path={path} element={<Placeholder title={label}/>}/>)}<Route path="/settings" element={<Placeholder title="Settings"/>}/></Routes></div></main>
  </div>
}
