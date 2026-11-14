import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import LoginLayout from './LoginLayout'
import './login.css'

export default function PlayerLogin() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  function submit(event: FormEvent) {
    event.preventDefault()
    setError('Player account authentication is not connected yet. Please contact cafe staff.')
  }

  return (
    <LoginLayout
      eyebrow="PC USER"
      title="Player login"
      subtitle="Sign in to your gaming account and continue your session."
      footer={
        <span>
          Need an account or password? Please contact the reception desk.
        </span>
      }
      topRight={
        <button
          type="button"
          className="login-link"
          onClick={() => navigate('/admin-login')}
        >
          Admin &rarr;
        </button>
      }
    >
      <form className="login-form" onSubmit={submit}>
        <label className="login-field">
          <span>Username / Customer ID</span>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Enter your username"
            autoFocus
            autoComplete="username"
            spellCheck={false}
          />
        </label>

        <label className="login-field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your password"
            autoComplete="current-password"
          />
        </label>

        {error && <p className="login-error">{error}</p>}

        <button type="submit" className="login-primary">
          Sign in
        </button>
      </form>
    </LoginLayout>
  )
}
