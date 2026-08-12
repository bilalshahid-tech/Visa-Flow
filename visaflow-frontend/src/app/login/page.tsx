'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch, setSession, getAccessToken } from '@/services/api';
import Link from 'next/link';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // If user is already authenticated, send them straight to dashboard
    if (getAccessToken()) {
      router.push('/dashboard');
    }
  }, [router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const data = await apiFetch<{
        accessToken: string;
        refreshToken: string;
        email: string;
        role: string;
        companyId: string;
        userId: string;
      }>('/auth/login', {
        method: 'POST',
        bodyData: { email, password },
      });

      setSession(
        data.accessToken,
        data.refreshToken,
        data.email,
        data.role,
        data.companyId,
        data.userId
      );

      router.push('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Verification failed. Please check credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div className="glass-card" style={styles.card}>
        <div style={styles.logoSec}>
          <span style={styles.logoGradient}>VisaFlow</span>
          <p style={styles.subtitle}>Modular Enterprise Platform</p>
        </div>

        <h2 style={styles.title}>Welcome Back</h2>
        <p style={styles.subtext}>Enter your credentials to enter the workspace</p>

        {error && <div style={styles.errorAlert}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input
              type="email"
              className="form-input"
              placeholder="name@company.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              type="password"
              className="form-input"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button
            type="submit"
            className="btn-primary"
            style={styles.submitBtn}
            disabled={loading}
          >
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>

        <div style={styles.footer}>
          New to VisaFlow?{' '}
          <Link href="/register" style={styles.link}>
            Create an Account
          </Link>
        </div>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    padding: '20px',
  },
  card: {
    width: '100%',
    maxWidth: '440px',
    padding: '40px 32px',
    borderRadius: '24px',
  },
  logoSec: {
    textAlign: 'center',
    marginBottom: '32px',
  },
  logoGradient: {
    fontSize: '2rem',
    fontWeight: '700',
    fontFamily: 'var(--font-display)',
    background: 'linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%)',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    letterSpacing: '-0.03em',
  },
  subtitle: {
    fontSize: '0.75rem',
    color: 'var(--text-dark)',
    textTransform: 'uppercase',
    letterSpacing: '0.15em',
    marginTop: '4px',
  },
  title: {
    fontSize: '1.5rem',
    fontWeight: '600',
    marginBottom: '8px',
    textAlign: 'center',
  },
  subtext: {
    color: 'var(--text-muted)',
    fontSize: '0.875rem',
    marginBottom: '24px',
    textAlign: 'center',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
  },
  submitBtn: {
    marginTop: '12px',
    padding: '14px',
  },
  errorAlert: {
    background: 'rgba(239, 68, 68, 0.1)',
    border: '1px solid rgba(239, 68, 68, 0.2)',
    borderRadius: '10px',
    color: 'var(--color-danger)',
    fontSize: '0.85rem',
    padding: '12px',
    marginBottom: '20px',
    textAlign: 'center',
    fontWeight: '500',
  },
  footer: {
    marginTop: '28px',
    textAlign: 'center',
    fontSize: '0.875rem',
    color: 'var(--text-muted)',
  },
  link: {
    color: 'var(--primary)',
    textDecoration: 'none',
    fontWeight: '600',
    marginLeft: '4px',
  },
};
