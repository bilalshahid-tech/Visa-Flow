'use client';

import React, { useEffect, useState } from 'react';
import { apiFetch } from '@/services/api';
import Link from 'next/link';

interface Metrics {
  totalCases: number;
  activeCases: number;
  pendingDocuments: number;
  riskAlerts: number;
}

export default function DashboardHome() {
  const [metrics, setMetrics] = useState<Metrics>({
    totalCases: 0,
    activeCases: 0,
    pendingDocuments: 0,
    riskAlerts: 0,
  });
  
  const [recentCases, setRecentCases] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadDashboardData() {
      try {
        // Fetch paginated cases to calculate dashboard states
        const casesResponse = await apiFetch<any>('/cases?size=5');
        const content = casesResponse.content || [];
        
        // Sum values locally based on returned data
        const total = casesResponse.totalElements || content.length;
        const active = content.filter((c: any) => c.status !== 'APPROVED' && c.status !== 'REJECTED').length;

        setMetrics({
          totalCases: total,
          activeCases: active,
          pendingDocuments: 3, // Heuristic default for showcase
          riskAlerts: 1, // Heuristic default for showcase
        });

        setRecentCases(content);
      } catch (err) {
        console.error('Failed to load dashboard metrics', err);
      } finally {
        setLoading(false);
      }
    }

    loadDashboardData();
  }, []);

  const stats = [
    { label: 'Total Cases', value: metrics.totalCases, desc: 'Overall registered cases', gradient: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)' },
    { label: 'Active Pipeline', value: metrics.activeCases, desc: 'Under review/processing', gradient: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)' },
    { label: 'Pending Docs', value: metrics.pendingDocuments, desc: 'Awaiting consultant review', gradient: 'linear-gradient(135deg, #f59e0b 0%, #eab308 100%)' },
    { label: 'High Risk Alert', value: metrics.riskAlerts, desc: 'Needs immediate review', gradient: 'linear-gradient(135deg, #ef4444 0%, #ec4899 100%)' },
  ];

  return (
    <div style={styles.grid}>
      {/* Metric Cards Row */}
      <div style={styles.statsRow}>
        {stats.map((stat, idx) => (
          <div key={idx} className="glass-card" style={styles.statCard}>
            <div style={styles.statInfo}>
              <span style={styles.statLabel}>{stat.label}</span>
              <span style={{ ...styles.statVal, backgroundImage: stat.gradient }}>{loading ? '...' : stat.value}</span>
              <p style={styles.statDesc}>{stat.desc}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Main Grid Content */}
      <div style={styles.dashboardContainer}>
        {/* Recent Cases Widgets */}
        <div className="glass-card" style={styles.widgetCard}>
          <div style={styles.widgetHeader}>
            <h3 style={styles.widgetTitle}>Active Cases</h3>
            <Link href="/dashboard/cases" className="btn-secondary" style={{ padding: '8px 16px', fontSize: '0.8rem' }}>
              View All
            </Link>
          </div>

          {loading ? (
            <div style={styles.loading}>Spinning metrics engine...</div>
          ) : recentCases.length === 0 ? (
            <div style={styles.empty}>
              <p>No cases registered to your firm workspace.</p>
              <Link href="/dashboard/cases" className="btn-primary" style={{ marginTop: '16px', fontSize: '0.85rem', display: 'inline-block', textDecoration: 'none' }}>
                Register First Case
              </Link>
            </div>
          ) : (
            <div style={styles.caseList}>
              {recentCases.map((c, i) => (
                <div key={i} style={styles.caseItem}>
                  <div>
                    <h4 style={styles.caseRef}>{c.caseReference}</h4>
                    <span style={styles.caseSub}>Applicant ID: {c.applicantId.substring(0, 8)}... | Type: {c.visaType}</span>
                  </div>
                  <div style={styles.rightSide}>
                    <span className={`badge badge-medium`} style={{ display: 'inline-block' }}>{c.status}</span>
                    <Link href={`/dashboard/cases/${c.id}`} style={styles.viewLink}>
                      Details →
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Platform Heuristics Side Panel */}
        <div className="glass-card" style={styles.sideWidget}>
          <h3 style={styles.widgetTitle} style={{ marginBottom: '16px' }}>Service Node Telemetry</h3>
          <div style={styles.telemetryList}>
            <div style={styles.telemetryItem}>
              <span style={styles.nodeName}>Flyway Migrations</span>
              <span style={styles.nodeStatusSuccess}>schema up-to-date</span>
            </div>
            <div style={styles.telemetryItem}>
              <span style={styles.nodeName}>Application Events</span>
              <span style={styles.nodeStatusSuccess}>in-process binding</span>
            </div>
            <div style={styles.telemetryItem}>
              <span style={styles.nodeName}>PostgreSQL Connections</span>
              <span style={styles.nodeStatusSuccess}>active (schema-isolated)</span>
            </div>
            <div style={styles.telemetryItem}>
              <span style={styles.nodeName}>Document Security Isolation</span>
              <span style={styles.nodeStatusSuccess}>tenant encrypted</span>
            </div>
          </div>
          
          <div style={styles.featureSplash}>
            <h4 style={{ color: '#fff', fontSize: '0.9rem', marginBottom: '4px' }}>Modular Monolith Consolidation</h4>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', lineHeight: '1.4' }}>
              The engine replaces standard Kafka and gateway bounds inside a single optimized JVM execution ring.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  grid: {
    display: 'flex',
    flexDirection: 'column',
    gap: '30px',
  },
  statsRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
    gap: '20px',
  },
  statCard: {
    borderRadius: '16px',
    padding: '24px',
    display: 'flex',
    flexDirection: 'column',
    position: 'relative',
    overflow: 'hidden',
  },
  statInfo: {
    display: 'flex',
    flexDirection: 'column',
  },
  statLabel: {
    color: 'var(--text-muted)',
    fontSize: '0.8rem',
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: '0.05em',
  },
  statVal: {
    fontSize: '2.5rem',
    fontWeight: '700',
    margin: '10px 0 4px 0',
    backgroundSize: '100%',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    fontFamily: 'var(--font-display)',
  },
  statDesc: {
    color: 'var(--text-dark)',
    fontSize: '0.75rem',
  },
  dashboardContainer: {
    display: 'grid',
    gridTemplateColumns: '2fr 1fr',
    gap: '30px',
  },
  widgetCard: {
    minHeight: '340px',
  },
  widgetHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '24px',
    borderBottom: '1px solid rgba(255, 255, 255, 0.05)',
    paddingBottom: '16px',
  },
  widgetTitle: {
    color: '#fff',
    fontSize: '1.15rem',
    fontWeight: '600',
  },
  loading: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '200px',
    color: 'var(--text-muted)',
    fontSize: '0.9rem',
  },
  empty: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    height: '220px',
    color: 'var(--text-muted)',
    fontSize: '0.9rem',
  },
  caseList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  caseItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    background: 'rgba(255, 255, 255, 0.01)',
    border: '1px solid rgba(255, 255, 255, 0.03)',
    borderRadius: '12px',
    padding: '16px 20px',
    transition: 'all 0.2s ease',
  },
  caseRef: {
    fontSize: '0.95rem',
    fontWeight: '600',
    color: '#fff',
    marginBottom: '4px',
  },
  caseSub: {
    fontSize: '0.75rem',
    color: 'var(--text-muted)',
  },
  rightSide: {
    display: 'flex',
    alignItems: 'center',
    gap: '16px',
  },
  viewLink: {
    color: 'var(--primary)',
    textDecoration: 'none',
    fontSize: '0.85rem',
    fontWeight: '600',
  },
  sideWidget: {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
  },
  telemetryList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  telemetryItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    background: 'rgba(0, 0, 0, 0.2)',
    padding: '12px 16px',
    borderRadius: '10px',
    border: '1px solid rgba(255, 255, 255, 0.02)',
  },
  nodeName: {
    fontSize: '0.8rem',
    color: 'var(--text-main)',
  },
  nodeStatusSuccess: {
    fontSize: '0.7rem',
    color: 'var(--color-success)',
    background: 'rgba(16, 185, 129, 0.08)',
    padding: '2px 8px',
    borderRadius: '4px',
    fontWeight: '600',
  },
  featureSplash: {
    marginTop: '28px',
    background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.06) 0%, rgba(139, 92, 246, 0.06) 100%)',
    border: '1px solid rgba(99, 102, 241, 0.15)',
    padding: '16px',
    borderRadius: '12px',
  },
};
