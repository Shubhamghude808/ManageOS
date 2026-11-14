import { useEffect, useMemo, useState } from 'react'
import { Clock3, Computer, Play, Pause, Square, Wifi, WifiOff } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { getAgentHeartbeat, getAgentSession } from '../services/agentApi'
import './player.css'

function formatRemaining(seconds: number): string {
  const value = Math.max(0, Math.floor(seconds))
  const h = String(Math.floor(value / 3600)).padStart(2, '0')
  const m = String(Math.floor((value % 3600) / 60)).padStart(2, '0')
  const s = String(value % 60).padStart(2, '0')
  return `${h}:${m}:${s}`
}

function formatClock(iso: string | null | undefined): string {
  if (!iso) return '—'
  const d = new Date(iso)
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleTimeString()
}

export default function PlayerHUD() {
  const session = useQuery({
    queryKey: ['agent-session'],
    queryFn: getAgentSession,
    refetchInterval: 5000,
    retry: 1,
  })
  const heartbeat = useQuery({
    queryKey: ['agent-heartbeat'],
    queryFn: getAgentHeartbeat,
    refetchInterval: 15000,
    retry: 1,
  })

  const [now, setNow] = useState(Date.now())
  useEffect(() => {
    const id = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(id)
  }, [])

  const data = session.data
  const elapsed = useMemo(() => {
    if (!data || data.status !== 'ACTIVE') return 0
    const updated = session.dataUpdatedAt ? session.dataUpdatedAt : Date.now()
    return Math.max(0, Math.floor((now - updated) / 1000))
  }, [data, now, session.dataUpdatedAt])

  const remaining = useMemo(() => {
    if (!data) return 0
    return Math.max(0, data.remainingSeconds - elapsed)
  }, [data, elapsed])

  const statusLabel: Record<string, string> = {
    ACTIVE: 'In session',
    PAUSED: 'Paused',
    COMPLETED: 'Completed',
    CANCELLED: 'Cancelled',
  }

  return (
    <div className="hud">
      <div className="hud-top">
        <div className="hud-brand">
          <Computer size={18} />
          <div>
            <span className="hud-cafe">CafeOS</span>
            <span className="hud-pc">{data?.pcName ?? 'PC not connected'}</span>
          </div>
        </div>
        <div className="hud-status">
          {heartbeat.data ? <Wifi size={15} /> : <WifiOff size={15} />}
          <span>{heartbeat.data ? 'Agent online' : 'Agent offline'}</span>
        </div>
      </div>

      <div className="hud-main">
        <div className="hud-timer">
          <div className="hud-timer-label">
            <Clock3 size={14} />
            <span>{data ? statusLabel[data.status] ?? data.status : 'No active session'}</span>
          </div>
          <div className={`hud-timer-value ${remaining < 300 && data?.status === 'ACTIVE' ? 'low' : ''}`}>
            {data ? formatRemaining(remaining) : '—'}
          </div>
          <div className="hud-timer-meta">
            <span>Started {formatClock(data?.startedAt)}</span>
            <span>Expires {formatClock(data?.expiresAt)}</span>
          </div>
        </div>

        <div className="hud-info">
          <div className="hud-card">
            <span>PC</span>
            <strong>{data?.pcName ?? '—'}</strong>
          </div>
          <div className="hud-card">
            <span>Plan</span>
            <strong>{data?.pricingPlanName ?? '—'}</strong>
          </div>
          <div className="hud-card">
            <span>Rate</span>
            <strong>
              {data ? `${data.currency} ${Number(data.hourlyRate).toFixed(2)}/hr` : '—'}
            </strong>
          </div>
          <div className="hud-card">
            <span>Amount due</span>
            <strong>
              {data ? `${data.currency} ${Number(data.amountDue ?? 0).toFixed(2)}` : '—'}
            </strong>
          </div>
        </div>
      </div>

      <div className="hud-actions">
        <button className="hud-btn" disabled>
          <Pause size={14} /> Pause
        </button>
        <button className="hud-btn" disabled>
          <Play size={14} /> Resume
        </button>
        <button className="hud-btn danger" disabled>
          <Square size={12} /> End session
        </button>
        <span className="hud-note">
          Session controls are managed by the cafe staff. Your timer follows the
          server&rsquo;s authoritative remaining time.
        </span>
      </div>
    </div>
  )
}