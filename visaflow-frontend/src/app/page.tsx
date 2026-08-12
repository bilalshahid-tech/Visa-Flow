'use client';

import React from 'react';
import Link from 'next/link';

export default function LandingPage() {
  return (
    <div style={styles.container}>
      {/* Decorative gradient blur rings */}
      <div style={styles.ringLeft}></div>
      <div style={styles.ringRight}></div>

      {/* Navigation Header */}
      <header style={styles.header}>
        <div style={styles.logoSec}>
          <span style={styles.logoText}>VisaFlow</span>
          <span style={styles.tag}>Modular Monolith</span>
        </div>
        <div style={styles.navActions}>
          <Link href="/login" style={styles.logoLink} className="btn-secondary">
            Sign In
          </Link>
          <Link href="/register" style={styles.logoLink} className="btn-primary">
            Register Firm
          </Link>
        </div>
      </header>

      {/* Hero Body */}
      <section style={styles.hero}>
        <h1 style={styles.title}>
          Visa Consultancy Operational <br />
          <span style={styles.titleGradient}>Intelligence Platform</span>
        </h1>
        <p style={styles.subtitle}>
          Simplify core client workflows, automate heuristic risk evaluation, and manage document compliance inside an isolated single-tenant modular runtime environment.
        </p>

        <div style={styles.ctaRow}>
          <Link href="/register" className="btn-primary" style={{ padding: '16px 36px', fontSize: '1.05rem', textDecoration: 'none' }}>
            Initialize Workspace
          </Link>
          <Link href="/login" className="btn-secondary" style={{ padding: '16px 36px', fontSize: '1.05rem', textDecoration: 'none' }}>
            Access Environment
          </Link>
        </div>
      </section>

      {/* Features Overview */}
      <section style={styles.featuresSection}>
        <div className="glass-card" style={styles.featureCard}>
          <div style={styles.iconCircle}>📂</div>
          <h3 style={styles.featTitle}>Case Lifecycle Management</h3>
          <p style={styles.featDesc}>
            Log client folders, track Stage transitions (Created to Approved), and capture real-time system audit timelines.
          </p>
        </div>

        <div className="glass-card" style={styles.featureCard}>
          <div style={styles.iconCircle}>⚡</div>
          <h3 style={styles.featTitle}>Heuristic Threat Tracker</h3>
          <p style={styles.featDesc}>
            Calculate case risk metrics instantly based on missing required document files and verification backlogs.
          </p>
        </div>

        <div className="glass-card" style={styles.featureCard}>
          <div style={styles.iconCircle}>🔒</div>
          <h3 style={styles.featTitle}>Multi-Tenant Isolation</h3>
          <p style={styles.featDesc}>
            Separate tenant scope dynamically using JWT claims to enforce PostgreSQL database schema boundaries automatically.
          </p>
        </div>
      </section>

      {/* Footer */}
      <footer style={styles.footer}>
        <p>© 2026 VisaFlow Solutions. Powered by Spring Boot 3.4.1 Modular Monolith.</p>
      </footer>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    padding: '40px max(40px, calc((100vw - 1200px)/2))',
    position: 'relative',
    overflow: 'hidden',
  },
  ringLeft: {
    position: 'absolute',
    top: '-300px',
    left: '-300px',
    width: '600px',
    height: '600px',
    borderRadius: '50%',
    background: 'radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, rgba(0,0,0,0) 70%)',
    pointerEvents: 'none',
    zIndex: -1,
  },
  ringRight: {
    position: 'absolute',
    bottom: '-200px',
    right: '-200px',
    width: '600px',
    height: '600px',
    borderRadius: '50%',
    background: 'radial-gradient(circle, rgba(139, 92, 246, 0.08) 0%, rgba(0,0,0,0) 70%)',
    pointerEvents: 'none',
    zIndex: -1,
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    zIndex: 2,
  },
  logoSec: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
  },
  logoText: {
    fontFamily: 'var(--font-display)',
    fontSize: '1.75rem',
    fontWeight: '700',
    background: 'linear-gradient(135deg, #fff 40%, var(--primary) 100%)',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    letterSpacing: '-0.03em',
  },
  tag: {
    fontSize: '0.65rem',
    fontWeight: '700',
    color: 'var(--text-dark)',
    border: '1px solid rgba(255,255,255,0.08)',
    padding: '3px 8px',
    borderRadius: '4px',
    textTransform: 'uppercase',
    letterSpacing: '0.05em',
  },
  navActions: {
    display: 'flex',
    gap: '16px',
  },
  logoLink: {
    padding: '10px 20px',
    fontSize: '0.875rem',
    textDecoration: 'none',
  },
  hero: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    textAlign: 'center',
    padding: '80px 20px 40px 20px',
    zIndex: 2,
  },
  title: {
    fontSize: 'clamp(2.5rem, 5vw, 3.75rem)',
    fontWeight: '700',
    lineHeight: 1.15,
    fontFamily: 'var(--font-display)',
  },
  titleGradient: {
    background: 'linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%)',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
  },
  subtitle: {
    color: 'var(--text-muted)',
    fontSize: 'clamp(0.95rem, 2vw, 1.1rem)',
    maxWidth: '700px',
    lineHeight: 1.6,
    marginTop: '24px',
  },
  ctaRow: {
    display: 'flex',
    gap: '16px',
    marginTop: '40px',
  },
  featuresSection: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
    gap: '24px',
    marginTop: '60px',
    zIndex: 2,
  },
  featureCard: {
    borderRadius: '20px',
    padding: '32px',
    background: 'rgba(255, 255, 255, 0.015)',
  },
  iconCircle: {
    width: '48px',
    height: '48px',
    borderRadius: '12px',
    background: 'rgba(255,255,255,0.03)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '1.25rem',
    marginBottom: '20px',
  },
  featTitle: {
    color: '#fff',
    fontSize: '1.1rem',
    fontWeight: '600',
    marginBottom: '8px',
  },
  featDesc: {
    color: 'var(--text-muted)',
    fontSize: '0.85rem',
    lineHeight: '1.5',
  },
  footer: {
    textAlign: 'center',
    paddingTop: '60px',
    fontSize: '0.8rem',
    color: 'var(--text-dark)',
    borderTop: '1px solid rgba(255,255,255,0.02)',
    marginTop: '60px',
  },
};
