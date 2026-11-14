import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { clearApiCredentials, setApiCredentials } from '../services/api'
import LoginLayout from './LoginLayout'
import './login.css'

/**
 * Admin login.
 *
 * Reuses the existing HTTP Basic credentials already used throughout the app
 * (setApiCredentials / hasApiCredentials in services/api.ts). Nothing about
 * the backend contract changes: the same username/password that unlocks the
 * Gaming PCs and Sessions pages unlocks the admin dashboard.
 */
export default function AdminLogin() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  function submit(event: FormEvent) {
    event.preventDefault()
    if (!username.trim() || !password) {
      setError('Username and password are required.')
      return
    }
    setBusy(true)
    setError('')
    setApiCredentials(username.trim(), password)
    // Probe the real staff endpoint. If the credentials are wrong, the API
    // calls will fail and we roll the credentials back.
    import('../services/api').then(({ getBranches }) =>
      getBranches()
        .then(() => {
          navigate('/admin', { replace: true })
        })
        .catch(() => {
          clearApiCredentials()
          setError('Could not sign in. Check the username and password shown in the backend console.')
        })
        .finally(() => setBusy(false)),
    )
  }

  return (
    <LoginLayout
      eyebrow="STAFF"
      title="Admin login"
      subtitle="Sign in with the CafeOS staff credentials to open the control room."
      footer={
        <span>
          Credentials are the Spring Security account configured in the backend
          <code>.env</code>. They are held in memory for this browser session only.
        </span>
      }
      topRight={
        <button type="button" className="login-link" onClick={() => navigate('/login')}>
          Player &rarr;
        </button>
      }
    >
      <form className="login-form" onSubmit={submit}>
        <label className="login-field">
          <span>Username</span>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoFocus
            spellCheck={false}
            autoCapitalize="off"
          />
        </label>
        <label className="login-field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            spellCheck={false}
          />
        </label>
        {error && <p className="login-error">{error}</p>}
        <button type="submit" className="login-primary" disabled={busy}>
          {busy ? 'Signing in&hellip;' : 'Sign in'}
        </button>
      </form>
    </LoginLayout>
  )
}

