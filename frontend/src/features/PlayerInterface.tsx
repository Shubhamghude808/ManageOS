import PlayerHUD from './PlayerHUD'
import Taskbar from './Taskbar'
import './player.css'

/**
 * Player interface.
 *
 * The player view is intentionally minimal: the HUD renders the real session
 * returned by /api/agent/session and the taskbar shows the current PC plus
 * clearly-labelled pending telemetry slots. Session controls (pause/resume/
 * end) are disabled because only the cafe staff can drive them through the
 * admin dashboard. This keeps the player experience honest: it reflects real
 * server state and never pretends to expose functionality that does not exist.
 */
export default function PlayerInterface() {
  return (
    <div className="player-shell">
      <div className="player-bg" aria-hidden="true" />
      <div className="player-content">
        <PlayerHUD />
      </div>
      <Taskbar />
    </div>
  )
}