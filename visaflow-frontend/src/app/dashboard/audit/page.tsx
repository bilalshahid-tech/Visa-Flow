'use client';

import React, { useEffect, useState } from 'react';
import { apiFetch } from '@/services/api';
import { useRouter } from 'next/navigation';

interface AuditLog {
  id: string;
  companyId: string;
  actorId: string;
  actorEmail: string;
  action: string;
  entityType: string;
  entityId: string;
  details: string;
  createdAt: string;
}

export default function AuditLogsPage() {
  const router = useRouter();
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Search parameters
  const [eType, setEType] = useState('');
  const [eId, setEId] = useState('');

  async function loadLogs() {
    setLoading(true);
    setError('');
    try {
      let endpoint = '/audit?size=100';
      if (eType && eId) {
        endpoint = `/audit/entity/${eType.toUpperCase()}/${eId}?size=100`;
      }
      
      const data = await apiFetch<any>(endpoint);
      setLogs(data.content || []);
    } catch (err: any) {
      if (err.status === 403) {
        setError('Access Denied. Only workspace administrators can view security logs.');
      } else {
        setError(err.message || 'Failed to retrieve audit trail.');
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    // Audit logs are retrieved on load only for ADMIN role checked by routing shell
    const role = localStorage.getItem('vf_role');
    if (role !== 'ADMIN') {
      setError('Access Denied. Redirecting to workspace dashboard...');
      setTimeout(() => {
        router.push('/dashboard');
      }, 3000);
      setLoading(false);
      return;
    }
    loadLogs();
  }, [router]);

  const handleFilterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadLogs();
  };

  return (
    <div style={styles.container}>
      <form onSubmit={handleFilterSubmit} style={styles.filterBar} className="glass-card">
        <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
          <label className="form-label">Entity Category</label>
          <select
            className="form-input"
            style={{ background: '#09090b', color: '#fff' }}
            value={eType}
            onChange={(e) => setEType(e.target.value)}
          >
            <option value="">All Operations</option>
            <option value="CASE">CASE (Case Modifications)</option>
            <option value="DOCUMENT">DOCUMENT (Upload / Verify)</option>
            <option value="USER">USER (Session Changes)</option>
          </select>
        </div>

        <div className="form-group" style={{ flex: 2, marginBottom: 0 }}>
          <label className="form-label">Reference ID (UUID)</label>
          <input
            type="text"
            className="form-input"
            placeholder="e.g. 5ba184b2-..."
            value={eId}
            onChange={(e) => setEId(e.target.value)}
          />
        </div>

        <button type="submit" className="btn-primary" style={{ height: '46px', alignSelf: 'flex-end' }}>
          Apply Filter
        </button>
      </form>

      {error ? (
        <div style={styles.errorAlert}>{error}</div>
      ) : (
        <div className="glass-card" style={styles.listCard}>
          {loading ? (
            <div style={styles.loading}>Retrieving security streams...</div>
          ) : logs.length === 0 ? (
            <div style={styles.empty}>
              <p>No audit trail logs match specified filters.</p>
            </div>
          ) : (
            <table style={styles.table}>
              <thead>
                <tr style={styles.tableHeaderRow}>
                  <th style={styles.th}>Timestamp</th>
                  <th style={styles.th}>Actor Email</th>
                  <th style={styles.th}>Action</th>
                  <th style={styles.th}>Entity (Type/ID)</th>
                  <th style={styles.th}>Event details</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <tr key={log.id} style={styles.tableRow}>
                    <td style={styles.tdTime}>{new Date(log.createdAt).toLocaleString()}</td>
                    <td style={styles.tdActor}>{log.actorEmail}</td>
                    <td style={styles.td}>
                      <span className="badge badge-low" style={{ background: 'rgba(99, 102, 241, 0.1)' }}>{log.action}</span>
                    </td>
                    <td style={styles.tdEntity}>
                      <strong>{log.entityType}</strong>
                      <p style={{ fontSize: '0.7rem', color: 'var(--text-dark)' }}>{log.entityId.substring(0, 8)}...</p>
                    </td>
                    <td style={{ ...styles.td, color: 'var(--text-muted)' }}>{log.details}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    gap: '24px',
  },
  filterBar: {
    display: 'flex',
    gap: '16px',
    alignItems: 'center',
    padding: '20px 24px',
  },
  listCard: {
    padding: '0',
    overflow: 'hidden',
  },
  loading: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '240px',
    color: 'var(--text-muted)',
  },
  empty: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '240px',
    color: 'var(--text-muted)',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
    textAlign: 'left',
  },
  tableHeaderRow: {
    borderBottom: '1px solid rgba(255, 255, 255, 0.05)',
    background: 'rgba(255, 255, 255, 0.01)',
  },
  th: {
    padding: '16px 24px',
    fontSize: '0.8rem',
    textTransform: 'uppercase',
    color: 'var(--text-muted)',
    fontWeight: '600',
  },
  tableRow: {
    borderBottom: '1px solid rgba(255, 255, 255, 0.02)',
  },
  tdTime: {
    padding: '16px 24px',
    fontSize: '0.825rem',
    color: 'var(--text-muted)',
  },
  tdActor: {
    padding: '16px 24px',
    fontWeight: '600',
    color: '#fff',
    fontSize: '0.875rem',
  },
  tdEntity: {
    padding: '16px 24px',
    fontSize: '0.825rem',
  },
  td: {
    padding: '16px 24px',
    fontSize: '0.85rem',
    color: 'var(--text-main)',
  },
  errorAlert: {
    background: 'rgba(239, 68, 68, 0.1)',
    border: '1px solid rgba(239, 68, 68, 0.2)',
    borderRadius: '10px',
    color: 'var(--color-danger)',
    padding: '16px',
    textAlign: 'center',
    fontWeight: '500',
  },
};
