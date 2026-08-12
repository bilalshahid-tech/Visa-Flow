'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { getAccessToken, clearSession } from '@/services/api';
import Link from 'next/link';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const currentPath = usePathname();
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('');
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const token = getAccessToken();
    if (!token) {
      router.push('/login');
      return;
    }
    setEmail(localStorage.getItem('vf_email') || 'user@visaflow.com');
    setRole(localStorage.getItem('vf_role') || 'CONSULTANT');
  }, [router]);

  const handleLogout = () => {
    clearSession();
  };

  if (!mounted) return null;

  const isAdmin = role === 'ADMIN';

  const menuItems = [
    { name: 'Dashboard', path: '/dashboard', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
    { name: 'Cases', path: '/dashboard/cases', icon: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z' },
  ];

  if (isAdmin) {
    menuItems.push({ name: 'Audit Logs', path: '/dashboard/audit', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01' });
  }

  return (
    <div style={styles.layout}>
      {/* Sidebar navigation */}
      <aside className="glass-card" style={styles.sidebar}>
        <div style={styles.logoSec}>
          <span style={styles.logoText}>VisaFlow</span>
          <div style={styles.badge}>{role}</div>
        </div>

        <nav style={styles.nav}>
          {menuItems.map((item) => {
            const isActive = currentPath === item.path || (item.path !== '/dashboard' && currentPath.startsWith(item.path));
            return (
              <Link key={item.name} href={item.path} style={{
                ...styles.navLink,
                ...(isActive ? styles.navLinkActive : {}),
              }}>
                <svg style={styles.navIcon} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={item.icon} />
                </svg>
                {item.name}
              </Link>
            );
          })}
        </nav>

        <div style={styles.userSection}>
          <div style={styles.userInfo}>
            <div style={styles.userIcon}>
              {email.charAt(0).toUpperCase()}
            </div>
            <div style={styles.userDetails}>
              <div style={styles.userEmail}>{email}</div>
              <div style={styles.userRole}>Active Session</div>
            </div>
          </div>
          <button onClick={handleLogout} className="btn-secondary" style={styles.logoutBtn}>
            <svg style={{ width: '16px', height: '16px' }} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div style={styles.mainContainer}>
        <header style={styles.header}>
          <div style={styles.headerTitle}>
            {currentPath === '/dashboard' && 'Platform Overview'}
            {currentPath === '/dashboard/cases' && 'Case Backlog'}
            {currentPath.startsWith('/dashboard/cases/') && 'Case Details'}
            {currentPath === '/dashboard/audit' && 'Security Audit Trails'}
          </div>
          <div style={styles.headerStatus}>
            <div style={styles.statusDot}></div>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Monolith Node Active</span>
          </div>
        </header>
        <section style={styles.content}>{children}</section>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  layout: {
    display: 'flex',
    minHeight: '100vh',
    background: 'radial-gradient(circle at 100% 100%, var(--bg-light) 0%, var(--bg-darker) 100%)',
  },
  sidebar: {
    width: '260px',
    height: 'calc(100vh - 40px)',
    position: 'sticky',
    top: '20px',
    left: '20px',
    margin: '20px 0 20px 20px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    borderRadius: '20px',
    border: '1px solid rgba(255, 255, 255, 0.04)',
    background: 'rgba(9, 9, 11, 0.4)',
    padding: '30px 20px 20px 20px',
    zIndex: 10,
  },
  logoSec: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
    marginBottom: '40px',
    paddingLeft: '10px',
  },
  logoText: {
    fontFamily: 'var(--font-display)',
    fontSize: '1.65rem',
    fontWeight: '700',
    background: 'linear-gradient(135deg, #fff 30%, var(--primary) 100%)',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    letterSpacing: '-0.02em',
  },
  badge: {
    background: 'rgba(99, 102, 241, 0.12)',
    border: '1px solid rgba(99, 102, 241, 0.25)',
    color: 'var(--primary)',
    fontSize: '0.65rem',
    fontWeight: '700',
    padding: '2px 8px',
    borderRadius: '4px',
    width: 'fit-content',
    letterSpacing: '0.05em',
  },
  nav: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    flexGrow: 1,
  },
  navLink: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    color: 'var(--text-muted)',
    textDecoration: 'none',
    fontSize: '0.925rem',
    fontWeight: '500',
    padding: '12px 16px',
    borderRadius: '12px',
    transition: 'all 0.2s ease',
  },
  navLinkActive: {
    background: 'rgba(255, 255, 255, 0.03)',
    color: '#fff',
    borderLeft: '3px solid var(--primary)',
    paddingLeft: '13px',
  },
  navIcon: {
    width: '18px',
    height: '18px',
  },
  userSection: {
    borderTop: '1px solid rgba(255, 255, 255, 0.05)',
    paddingTop: '20px',
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  userInfo: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  userIcon: {
    width: '36px',
    height: '36px',
    borderRadius: '50%',
    background: 'linear-gradient(135deg, var(--primary) 0%, var(--secondary) 100%)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: '700',
    fontSize: '0.9rem',
    color: '#fff',
  },
  userDetails: {
    overflow: 'hidden',
  },
  userEmail: {
    fontSize: '0.825rem',
    fontWeight: '600',
    color: 'var(--text-main)',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  userRole: {
    fontSize: '0.7rem',
    color: 'var(--text-dark)',
  },
  logoutBtn: {
    width: '100%',
    padding: '10px',
    fontSize: '0.85rem',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
    borderRadius: '10px',
    cursor: 'pointer',
  },
  mainContainer: {
    flexGrow: 1,
    padding: '40px',
    maxHeight: '105vh',
    overflowY: 'auto',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '32px',
    borderBottom: '1px solid rgba(255, 255, 255, 0.04)',
    paddingBottom: '20px',
  },
  headerTitle: {
    fontFamily: 'var(--font-display)',
    fontSize: '1.75rem',
    fontWeight: '700',
    letterSpacing: '-0.02em',
  },
  headerStatus: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    background: 'rgba(16, 185, 129, 0.05)',
    border: '1px solid rgba(16, 185, 129, 0.15)',
    padding: '6px 14px',
    borderRadius: '20px',
  },
  statusDot: {
    width: '8px',
    height: '8px',
    borderRadius: '50%',
    background: 'var(--color-success)',
    boxShadow: '0 0 10px var(--color-success)',
  },
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: '24px',
  },
};
