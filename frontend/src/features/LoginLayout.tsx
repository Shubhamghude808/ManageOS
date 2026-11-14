import { type ReactNode } from 'react'
import './login.css'

interface LoginLayoutProps {
  eyebrow: string
  title: string
  subtitle: string
  footer: ReactNode
  children: ReactNode
  topRight?: ReactNode
}

export default function LoginLayout({
  eyebrow,
  title,
  subtitle,
  footer,
  topRight,
  children,
}: LoginLayoutProps) {
  return (
    <div className="login-shell">
      <div className="login-grid">
        <section className="login-hero">
          <div className="login-brand">
            <div className="login-brand-mark">C</div>
            <div>
              <strong>CafeOS</strong>
              <span>CONTROL ROOM</span>
            </div>
          </div>
          <div className="login-tagline">
            <p>{eyebrow}</p>
            <h1>{title}</h1>
            <p className="login-subtitle">{subtitle}</p>
          </div>
          <div className="login-strip" aria-hidden="true">
            <span />
            <span />
            <span />
            <span />
          </div>
        </section>

        <section className="login-card">
          <div className="login-card-head">
            <div>
              <p className="login-eyebrow">{eyebrow}</p>
              <h2>{title}</h2>
            </div>
            {topRight}
          </div>
          {children}
          <div className="login-footer">{footer}</div>
        </section>
      </div>
    </div>
  )
}