import { useEffect, useState } from 'react'
import { Computer, LogOut, Moon, Wifi } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { clearAgentToken } from '../services/agentApi'
import { useQuery } from '@tanstack/react-query'
import { getAgentSession } from '../services/agentApi'
import './player.css'

export default function Taskbar() {
  const navigate = useNavigate()
  const session = useQuery({
    queryKey: ['agent-session'],
    queryFn: getAgentSession,
    refetchInterval: 5000,
    retry: 1,
  })
  const [clock, setClock] = useState(new Date())

  useEffect(() => {
    const id = window.setInterval(() => setClock(new Date()), 1000)
    return () => window.clearInterval(id)
  }, [])

  function logout() {
    clearAgentToken()
    navigate('/login', { replace: true })
  }

  const time = clock.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  const date = clock.toLocaleDateString([], { month: 'short', day: 'numeric' })

  return (
    <div className="taskbar">
      <div className="taskbar-start">
        <button className="taskbar-btn brand" onClick={() => navigate('/player')}>
          <Computer size={15} />
          <span>CafeOS</span>
        </button>
      </div>

      <div className="taskbar-center">
        <span className="taskbar-divider" />
        {session.data ? (
          <button className="taskbar-btn active" title="Current session">
            <span className="taskbar-dot live" />
            <span>{session.data.pcName}</span>
            <span className="taskbar-sub">{session.data.pricingPlanName}</span>
          </button>
        ) : (
          <span className="taskbar-empty">
            <span className="taskbar-dot idle" />
            No active session
          </span>
        )}
        <span className="taskbar-divider" />
        <span className="taskbar-pending" title="Game/app telemetry not yet reported by the agent">
          <span className="taskbar-dot idle" />
          Games &amp; apps
          <small>pending agent telemetry</small>
        </span>
      </div>

      <div className="taskbar-end">
        <button className="taskbar-btn" title="Dark mode" disabled>
          <Moon size={14} />
        </button>
        <button className="taskbar-btn" title="Connection" disabled>
          <Wifi size={14} />
        </button>
        <span className="taskbar-clock">
          <span>{time}</span>
          <small>{date}</small>
        </span>
        <button className="taskbar-btn danger" title="Log out" onClick={logout}>
          <LogOut size={14} />
          <span>Exit</span>
        </button>
      </div>
    </div>
  )
}
