'use client';

import React, { useEffect, useState } from 'react';
import { apiFetch } from '@/services/api';
import Link from 'next/link';

interface VisaCase {
  id: string;
  companyId: string;
  caseReference: string;
  status: string;
  visaTypeCode: string;
  visaTypeName: string;
  clientId: string;
  clientName: string;
  createdAt: string;
}

export default function CasesPage() {
  const [cases, setCases] = useState<VisaCase[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  async function loadCases() {
    setLoading(true);
    try {
      const url = statusFilter
        ? `/cases?status=${statusFilter}&size=50`
        : '/cases?size=50';
      const data = await apiFetch<any>(url);
      setCases(data.content || []);
    } catch (err: any) {
      setError(err.message || 'Failed to retrieve cases.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadCases();
  }, [statusFilter]);

  const friendlyStatus = (status: string) => {
    switch (status) {
      case 'DRAFT': return 'Draft';
      case 'DOCS_PENDING': return 'Docs Pending';
      case 'UNDER_REVIEW': return 'Under Review';
      case 'SUBMITTED': return 'Submitted';
      case 'APPROVED': return 'Approved';
      case 'REJECTED': return 'Rejected';
      case 'CLOSED': return 'Closed';
      default: return status;
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'APPROVED': return 'badge-low';
      case 'REJECTED': return 'badge-high';
      case 'SUBMITTED': return 'badge-critical';
      case 'UNDER_REVIEW':
      case 'DOCS_PENDING': return 'badge-medium';
      default: return 'badge-secondary';
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '1.8rem', color: '#fff', marginBottom: '4px' }}>Case Management</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Overview and status tracking for all visa consultancy applications
          </p>
        </div>
        <Link href="/dashboard/cases/new" className="btn-primary" style={{ textDecoration: 'none' }}>
          + Register New Case
        </Link>
      </div>

      {error && (
        <div style={{ padding: '12px 16px', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: '10px', color: 'var(--color-danger)', fontSize: '0.9rem' }}>
          {error}
        </div>
      )}

      {/* Filter Bar */}
      <div className="glass-card" style={{ padding: '16px 24px', display: 'flex', alignItems: 'center', gap: '16px' }}>
        <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: 600, textTransform: 'uppercase' }}>Filter by Status:</span>
        <select
          className="form-input"
          style={{ width: 'auto', minWidth: '180px', padding: '8px 14px', fontSize: '0.875rem', background: '#09090b', color: '#fff' }}
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">All Statuses</option>
          <option value="DRAFT">Draft</option>
          <option value="DOCS_PENDING">Documents Pending</option>
          <option value="UNDER_REVIEW">Under Review</option>
          <option value="SUBMITTED">Submitted</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="CLOSED">Closed</option>
        </select>
        {statusFilter && (
          <button className="btn-secondary" style={{ padding: '6px 12px', fontSize: '0.8rem' }} onClick={() => setStatusFilter('')}>
            Clear Filter
          </button>
        )}
      </div>

      {/* Backlog Table */}
      <div className="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            Loading case records…
          </div>
        ) : cases.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            No visa cases found.
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: 'rgba(255, 255, 255, 0.02)', borderBottom: '1px solid var(--glass-border)', color: 'var(--text-muted)', fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                <th style={{ padding: '16px 24px' }}>Reference</th>
                <th style={{ padding: '16px 24px' }}>Client</th>
                <th style={{ padding: '16px 24px' }}>Visa Type</th>
                <th style={{ padding: '16px 24px' }}>Status</th>
                <th style={{ padding: '16px 24px' }}>Created</th>
                <th style={{ padding: '16px 24px', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {cases.map((c) => (
                <tr key={c.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.03)', transition: 'background 0.2s ease' }}>
                  <td style={{ padding: '16px 24px', fontWeight: 600, color: '#fff' }}>
                    {c.caseReference}
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-main)' }}>
                    {c.clientName || '—'}
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-muted)' }}>
                    {c.visaTypeName || c.visaTypeCode || '—'}
                  </td>
                  <td style={{ padding: '16px 24px' }}>
                    <span className={`badge ${getStatusBadgeClass(c.status)}`}>
                      {friendlyStatus(c.status)}
                    </span>
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-dark)', fontSize: '0.85rem' }}>
                    {new Date(c.createdAt).toLocaleDateString()}
                  </td>
                  <td style={{ padding: '16px 24px', textAlign: 'right' }}>
                    <Link href={`/dashboard/cases/${c.id}`} className="btn-secondary" style={{ padding: '6px 12px', fontSize: '0.8rem', textDecoration: 'none' }}>
                      View Details →
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
