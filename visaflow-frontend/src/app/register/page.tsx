'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch, setSession } from '@/services/api';
import Link from 'next/link';
import PhoneInput, { getCountryCallingCode, Country } from 'react-phone-number-input';
import 'react-phone-number-input/style.css';

interface FlagProps {
  country: Country;
  countryName: string;
}

const CustomFlag = ({ country, countryName }: FlagProps) => {
  const callingCode = country ? getCountryCallingCode(country) : '';
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
      {country ? (
        <img
          src={`https://purecatamphetamine.github.io/country-flag-icons/3x2/${country}.svg`}
          alt={countryName}
          style={{ width: '20px', height: '15px', borderRadius: '2px' }}
        />
      ) : (
        <span style={{ fontSize: '1.1rem' }}>🌐</span>
      )}
      {callingCode && (
        <span style={{ color: 'var(--text-main)', fontSize: '0.85rem', fontWeight: 500 }}>
          +{callingCode}
        </span>
      )}
    </div>
  );
};

export default function RegisterPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [companyName, setCompanyName] = useState('');
  
  const [showPassword, setShowPassword] = useState(false);
  
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z\d]).{8,}$/;
    if (!passwordRegex.test(password)) {
      setError('Password must be at least 8 characters long and contain uppercase, lowercase, numbers, and special characters.');
      setLoading(false);
      return;
    }

    try {
      const data = await apiFetch<{
        accessToken: string;
        refreshToken: string;
        email: string;
        role: string;
        companyId: string;
        userId: string;
      }>('/auth/register', {
        method: 'POST',
        bodyData: {
          email,
          password,
          firstName,
          lastName,
          phoneNumber,
          companyName,
        },
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
      setError(err.message || 'Registration failed. Email might be in use.');
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

        <h2 style={styles.title}>Create Firm Account</h2>
        <p style={styles.subtext}>Register your company instance to begin onboarding clients</p>

        {error && <div style={styles.errorAlert}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formRow}>
            <div className="form-group" style={{ flex: 1 }}>
              <label className="form-label">First Name</label>
              <input
                type="text"
                className="form-input"
                placeholder="John"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
              />
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label className="form-label">Last Name</label>
              <input
                type="text"
                className="form-input"
                placeholder="Doe"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Company Name</label>
            <input
              type="text"
              className="form-input"
              placeholder="Apex Visa Consultancy"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input
              type="email"
              className="form-input"
              placeholder="admin@mycompany.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Phone Number</label>
            <PhoneInput
              placeholder="Enter phone number"
              value={phoneNumber}
              onChange={(val) => setPhoneNumber(val || '')}
              defaultCountry="US"
              international={false}
              flagComponent={CustomFlag}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Secure Password</label>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                className="form-input"
                placeholder="Min. 8 characters, mixed case, symbols"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                style={{ width: '100%', paddingRight: '46px' }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: 'absolute',
                  right: '12px',
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                  padding: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--text-muted)',
                  transition: 'color 0.2s',
                }}
                onMouseEnter={(e) => (e.currentTarget.style.color = 'var(--text-main)')}
                onMouseLeave={(e) => (e.currentTarget.style.color = 'var(--text-muted)')}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                ) : (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                )}
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="btn-primary"
            style={styles.submitBtn}
            disabled={loading}
          >
            {loading ? 'Creating Account...' : 'Get Started'}
          </button>
        </form>

        <div style={styles.footer}>
          Already registered?{' '}
          <Link href="/login" style={styles.link}>
            Sign In
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
    minHeight: '105vh',
    padding: '24px 20px',
  },
  card: {
    width: '100%',
    maxWidth: '520px',
    padding: '40px 36px',
    borderRadius: '24px',
  },
  logoSec: {
    textAlign: 'center',
    marginBottom: '28px',
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
    fontSize: '1.45rem',
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
  formRow: {
    display: 'flex',
    gap: '16px',
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
    marginTop: '24px',
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
