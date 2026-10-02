'use client';

import React, { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { apiFetch } from '@/services/api';

// ── Types ──────────────────────────────────────────────────────────────────────

interface StageInfo {
  id: string; sequenceOrder: number; name: string; description: string;
  isCurrent: boolean; isCompleted: boolean; enteredAt: string | null; completedAt: string | null;
}

interface StageChecklistItem {
  requirementId: string; documentType: string; displayName: string;
  mandatory: boolean; conditionallyMandatory: boolean; notes: string | null;
  displayOrder: number;
  documentId: string | null; documentStatus: string | null;
  originalFilename: string | null; reviewerNotes: string | null;
}

// Legacy flat checklist item (for old cases)
interface ChecklistItem {
  requirementId: string; documentClass: string; label: string;
  mandatory: boolean; displayOrder: number;
  documentId: string | null; documentStatus: string | null;
  originalFilename: string | null; reviewerNotes: string | null;
}

interface HistoryItem { fromStatus: string; toStatus: string; changedBy: string; note: string; changedAt: string; }
interface NoteItem { id: string; authorId: string; authorEmail: string; body: string; createdAt: string; }

interface CaseDetail {
  id: string; caseReference: string; status: string;
  allowedTransitions: string[];
  clientId?: string | null;
  clientName: string; clientPassportNumber: string; clientNationality: string;
  clientDateOfBirth: string; clientPhone: string; clientEmail: string;
  // New program fields
  visaProgramId: string | null; visaProgramName: string | null; jobRoleCategory: string | null;
  stages: StageInfo[] | null;
  currentStageId: string | null; currentStageName: string | null;
  currentStageChecklist: StageChecklistItem[] | null;
  // Legacy
  visaTypeName: string | null; visaTypeCode: string | null;
  checklist: ChecklistItem[]; checklistTotal: number; checklistUploaded: number;
  statusHistory: HistoryItem[]; notes: NoteItem[];
  createdAt: string; createdBy: string;
}

// Helper to detect MIME type from original filename
const getMimeFromFilename = (filename?: string | null) => {
  if (!filename) return 'application/pdf';
  const ext = filename.toLowerCase().split('.').pop();
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'].includes(ext || '')) {
    return `image/${ext === 'jpg' ? 'jpeg' : ext}`;
  }
  return 'application/pdf';
};

// ── Upload row component ────────────────────────────────────────────────────────

interface StageUploadRowProps {
  item: StageChecklistItem; isExpanded: boolean; isSaving: boolean;
  onOpenUpload: () => void; onCancelUpload: () => void;
  onSave: (file: File) => void; onView: () => void;
  readOnly?: boolean;
}

function StageUploadRow({ item, isExpanded, isSaving, onOpenUpload, onCancelUpload, onSave, onView, readOnly }: StageUploadRowProps) {
  const [file, setFile] = useState<File | null>(null);
  useEffect(() => { if (!isExpanded) setFile(null); }, [isExpanded]);

  const badge = docBadge(item.documentStatus);
  const effectivelyMandatory = item.mandatory || item.conditionallyMandatory;

  return (
    <div style={rowStyle}>
      <div style={{ flex: 1 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
          <span style={{ color: '#fff', fontSize: '0.875rem', fontWeight: 500 }}>{item.displayName}</span>
          {effectivelyMandatory && (
            <span style={{ fontSize: '0.65rem', color: 'var(--color-warning)', border: '1px solid rgba(245,158,11,0.3)', borderRadius: 4, padding: '1px 6px', fontWeight: 600 }}>
              {item.conditionallyMandatory && !item.mandatory ? '★ Conditional Required' : '★ Required'}
            </span>
          )}
        </div>
        {item.notes && <p style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: 3, fontStyle: 'italic' }}>ℹ {item.notes}</p>}
        {item.originalFilename && <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 3 }}>📎 {item.originalFilename}</p>}
        {item.reviewerNotes && <p style={{ fontSize: '0.75rem', color: 'var(--color-danger)', marginTop: 3, fontStyle: 'italic' }}>🔴 {item.reviewerNotes}</p>}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexShrink: 0 }}>
        <span style={{ padding: '3px 10px', borderRadius: 9999, background: badge.bg, color: badge.color, fontSize: '0.72rem', fontWeight: 600 }}>
          {badge.label}
        </span>
        {item.documentId && (
          <button className="btn-secondary" style={{ padding: '4px 10px', fontSize: '0.75rem' }} onClick={onView}>👁 View</button>
        )}
        {!readOnly && (
          !isExpanded ? (
            <button className="btn-secondary" style={{ padding: '4px 10px', fontSize: '0.75rem' }} onClick={onOpenUpload}>↑ Upload</button>
          ) : (
            <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <input type="file" accept="image/*,application/pdf"
                style={{ fontSize: '0.75rem', maxWidth: 150, color: 'var(--text-muted)' }}
                onChange={e => setFile(e.target.files?.[0] ?? null)} />
              <button className="btn-primary" style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                onClick={() => file && onSave(file)} disabled={!file || isSaving}>
                {isSaving ? '…' : 'Save'}
              </button>
              <button className="btn-secondary" style={{ padding: '4px 10px', fontSize: '0.75rem' }} onClick={onCancelUpload}>✕</button>
            </div>
          )
        )}
      </div>
    </div>
  );
}

const rowStyle: React.CSSProperties = {
  display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between',
  padding: '12px 14px', background: 'rgba(255,255,255,0.02)', borderRadius: 10,
  border: '1px solid rgba(255,255,255,0.04)', gap: 12,
};

const docBadge = (status: string | null) => {
  if (!status) return { label: 'Missing', bg: 'rgba(239,68,68,0.1)', color: 'var(--color-danger)' };
  if (status === 'APPROVED') return { label: 'Approved', bg: 'rgba(16,185,129,0.1)', color: 'var(--color-success)' };
  if (status === 'REJECTED') return { label: 'Rejected', bg: 'rgba(239,68,68,0.1)', color: 'var(--color-danger)' };
  return { label: 'Pending Review', bg: 'rgba(245,158,11,0.1)', color: 'var(--color-warning)' };
};

// ── Main page ───────────────────────────────────────────────────────────────────

export default function CaseDetailsPage() {
  const { id } = useParams() as { id: string };
  const [kase, setKase] = useState<CaseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');

  // Status modal
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [newStatus, setNewStatus] = useState('');
  const [statusNote, setStatusNote] = useState('');
  const [transitioning, setTransitioning] = useState(false);

  // Notes
  const [noteBody, setNoteBody] = useState('');
  const [addingNote, setAddingNote] = useState(false);

  // Upload state
  const [uploadingReqId, setUploadingReqId] = useState<string | null>(null);
  const [savingReqId, setSavingReqId] = useState('');

  // Viewed stage (for clicking past stages)
  const [viewedStageId, setViewedStageId] = useState<string | null>(null);
  const [viewedChecklist, setViewedChecklist] = useState<StageChecklistItem[]>([]);
  const [loadingViewedStage, setLoadingViewedStage] = useState(false);

  // Advance stage
  const [advancing, setAdvancing] = useState(false);

  // Preview modal
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [previewMime, setPreviewMime] = useState('');

  const load = async (silent = false) => {
    if (!silent) setLoading(true); else setRefreshing(true);
    try {
      const d = await apiFetch<CaseDetail>(`/cases/${id}`, { cache: 'no-store' } as any);
      setKase(d);
      if (d.allowedTransitions.length > 0) setNewStatus(d.allowedTransitions[0]);
      // Default: view current stage
      if (d.currentStageId) setViewedStageId(d.currentStageId);
    } catch (e: any) { setError(e.message || 'Failed to load case.'); }
    finally { setLoading(false); setRefreshing(false); }
  };

  useEffect(() => { if (id) load(); }, [id]);

  // When viewedStageId changes and it's not the current stage, fetch that stage's checklist
  useEffect(() => {
    if (!viewedStageId || !kase) return;
    if (viewedStageId === kase.currentStageId) {
      setViewedChecklist(kase.currentStageChecklist || []);
      return;
    }
    // Fetch checklist for a different (past) stage
    setLoadingViewedStage(true);
    apiFetch<StageChecklistItem[]>(`/cases/${id}/stages/${viewedStageId}/checklist`)
      .then(setViewedChecklist)
      .catch(() => setViewedChecklist([]))
      .finally(() => setLoadingViewedStage(false));
  }, [viewedStageId, kase]);

  const handleStatusTransition = async (e: React.FormEvent) => {
    e.preventDefault(); setTransitioning(true);
    try {
      const friendlyLabels: Record<string, string> = {
        DOCS_PENDING: 'Documents Pending', UNDER_REVIEW: 'Under Review', DRAFT: 'Draft',
        SUBMITTED: 'Submitted', APPROVED: 'Approved', REJECTED: 'Rejected', CLOSED: 'Closed'
      };
      const label = friendlyLabels[newStatus] || newStatus;
      const noteToSend = statusNote.trim() || `Status updated to ${label}`;
      await apiFetch(`/cases/${id}/status`, { method: 'PATCH', bodyData: { newStatus: newStatus, note: noteToSend } });
      setShowStatusModal(false); setStatusNote(''); await load();
    } catch (e: any) { setError(e.message || 'Transition failed.'); }
    finally { setTransitioning(false); }
  };

  const handleAddNote = async (e: React.FormEvent) => {
    e.preventDefault(); if (!noteBody.trim()) return; setAddingNote(true);
    try {
      await apiFetch(`/cases/${id}/notes`, { method: 'POST', bodyData: { body: noteBody } });
      setNoteBody(''); await load();
    } catch (e: any) { setError(e.message || 'Could not add note.'); }
    finally { setAddingNote(false); }
  };

  const handleUpload = async (reqId: string, file: File, isStageReq = false) => {
    setSavingReqId(reqId);
    const fd = new FormData();
    fd.append('file', file);
    if (isStageReq) fd.append('stageDocumentRequirementId', reqId);
    else fd.append('requirementId', reqId);
    try {
      await apiFetch(`/cases/${id}/documents`, { method: 'POST', body: fd });
      setUploadingReqId(null);
      await load(true);
    } catch (e: any) { setError(e.message || 'Upload failed.'); }
    finally { setSavingReqId(''); }
  };

  const handleView = async (docId: string, mime: string) => {
    try {
      const url = await apiFetch<string>(`/cases/${id}/documents/${docId}/view`);
      setPreviewUrl(url); setPreviewMime(mime || 'application/pdf');
    } catch (e: any) { setError(e.message || 'Could not load document.'); }
  };

  const handleAdvanceStage = async () => {
    setAdvancing(true); setError('');
    try {
      await apiFetch(`/cases/${id}/advance-stage`, { method: 'POST' });
      await load();
    } catch (e: any) { setError(e.message || 'Could not advance stage.'); }
    finally { setAdvancing(false); }
  };

  const statusColor = (s?: string | null) => {
    if (!s) return 'var(--text-muted)';
    if (s.toLowerCase().includes('approv')) return 'var(--color-success)';
    if (s.toLowerCase().includes('reject')) return 'var(--color-danger)';
    if (s.toLowerCase().includes('pending') || s.toLowerCase().includes('review')) return 'var(--color-warning)';
    if (s.toLowerCase().includes('draft')) return 'var(--text-dark)';
    return 'var(--primary)';
  };

  if (loading) return <div style={{ color: 'var(--text-muted)', padding: '60px', textAlign: 'center' }}>Loading case…</div>;
  if (!kase) return <div style={{ padding: '40px', textAlign: 'center' }}><p style={{ color: 'var(--color-danger)' }}>Case not found.</p><Link href="/dashboard/cases" className="btn-secondary" style={{ marginTop: 16, display: 'inline-block' }}>← Cases</Link></div>;

  const isNewStyleCase = !!kase.visaProgramId;
  const stages = kase.stages || [];
  const currentStage = stages.find(s => s.isCurrent);
  const viewedStage = stages.find(s => s.id === viewedStageId);
  const isViewingCurrentStage = viewedStageId === kase.currentStageId;

  // Determine if "Mark Stage Complete" should be enabled
  const currentChecklist = kase.currentStageChecklist || [];
  const allMandatoryUploaded = currentChecklist
    .filter(item => item.mandatory || item.conditionallyMandatory)
    .every(item => item.documentId !== null);
  const hasMoreStages = stages.some(s => s.sequenceOrder > (currentStage?.sequenceOrder ?? 0));

  // Legacy
  const legacyMandatoryUploaded = kase.checklist.filter(c => c.mandatory && c.documentId).length;
  const legacyMandatoryTotal = kase.checklist.filter(c => c.mandatory).length;
  const progressPct = kase.checklistTotal > 0 ? Math.round((kase.checklistUploaded / kase.checklistTotal) * 100) : 0;

  return (
    <div style={s.container}>
      {/* Header */}
      <div style={s.headerRow}>
        <Link href="/dashboard/cases" className="btn-secondary" style={{ padding: '8px 16px', fontSize: '0.85rem', textDecoration: 'none' }}>← Cases</Link>
        <div>
          <h2 style={{ color: '#fff', fontSize: '1.4rem' }}>{kase.caseReference}</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: 2 }}>
            {kase.visaProgramName || kase.visaTypeName}
            {kase.jobRoleCategory && <span style={{ marginLeft: 8, color: 'var(--color-warning)', fontSize: '0.75rem' }}>· {kase.jobRoleCategory}</span>}
          </p>
        </div>
        <span style={{ padding: '6px 16px', borderRadius: 9999, background: 'rgba(99,102,241,0.1)', color: statusColor(kase.status), fontWeight: 700, fontSize: '0.85rem', border: `1px solid ${statusColor(kase.status)}40` }}>
          {kase.status}
        </span>
        {refreshing && <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>Syncing…</span>}
      </div>

      {error && <div style={s.errorAlert}>{error}<button style={{ float: 'right', background: 'none', border: 'none', color: 'inherit', cursor: 'pointer' }} onClick={() => setError('')}>✕</button></div>}

      {/* ── Stage Stepper (new cases only) ─────────────────────────────────── */}
      {isNewStyleCase && stages.length > 0 && (
        <div className="glass-card" style={{ padding: '20px 24px' }}>
          <h3 style={{ ...s.sectionTitle, marginBottom: 20 }}>📋 Case Stages</h3>
          <div style={{ display: 'flex', alignItems: 'flex-start', gap: 0 }}>
            {stages.map((stage, idx) => {
              const isActive = stage.id === viewedStageId;
              return (
                <React.Fragment key={stage.id}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: 90, cursor: 'pointer' }}
                    onClick={() => setViewedStageId(stage.id)}>
                    <div style={{
                      width: 36, height: 36, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: '0.85rem', fontWeight: 700, border: '2px solid',
                      borderColor: stage.isCompleted ? 'var(--color-success)' : stage.isCurrent ? 'var(--primary)' : 'rgba(255,255,255,0.15)',
                      background: stage.isCompleted ? 'rgba(16,185,129,0.15)' : stage.isCurrent ? 'rgba(99,102,241,0.2)' : 'rgba(255,255,255,0.04)',
                      color: stage.isCompleted ? 'var(--color-success)' : stage.isCurrent ? 'var(--primary)' : 'rgba(255,255,255,0.3)',
                      boxShadow: isActive ? `0 0 0 3px ${stage.isCurrent ? 'rgba(99,102,241,0.3)' : stage.isCompleted ? 'rgba(16,185,129,0.3)' : 'rgba(255,255,255,0.1)'}` : 'none',
                      transition: 'all 0.2s ease',
                    }}>
                      {stage.isCompleted ? '✓' : stage.sequenceOrder}
                    </div>
                    <div style={{ marginTop: 8, textAlign: 'center', fontSize: '0.72rem', fontWeight: isActive ? 600 : 400, color: stage.isCompleted ? 'var(--color-success)' : stage.isCurrent ? '#fff' : 'var(--text-dark)', maxWidth: 80, lineHeight: 1.3 }}>
                      {stage.name}
                    </div>
                    {stage.isCurrent && <div style={{ marginTop: 4, fontSize: '0.65rem', color: 'var(--primary)', fontWeight: 600 }}>ACTIVE</div>}
                    {stage.isCompleted && stage.completedAt && <div style={{ marginTop: 2, fontSize: '0.62rem', color: 'var(--text-muted)' }}>{new Date(stage.completedAt).toLocaleDateString()}</div>}
                  </div>
                  {idx < stages.length - 1 && (
                    <div style={{ flex: 1, height: 2, marginTop: 17, background: stage.isCompleted ? 'rgba(16,185,129,0.4)' : 'rgba(255,255,255,0.08)' }} />
                  )}
                </React.Fragment>
              );
            })}
          </div>
        </div>
      )}

      <div style={s.grid}>
        {/* LEFT COLUMN */}
        <div style={s.leftCol}>
          {/* Client info */}
          <div className="glass-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h3 style={{ ...s.sectionTitle, marginBottom: 0 }}>👤 Client Information</h3>
              {kase.clientId && (
                <Link href={`/dashboard/clients/${kase.clientId}`} className="btn-secondary" style={{ padding: '4px 12px', fontSize: '0.78rem', textDecoration: 'none' }}>
                  View Client Profile →
                </Link>
              )}
            </div>
            <div style={s.infoGrid}>
              {[['Full Name', kase.clientName], ['Passport', kase.clientPassportNumber], ['Nationality', kase.clientNationality], ['Date of Birth', kase.clientDateOfBirth], ['Phone', kase.clientPhone || '—'], ['Email', kase.clientEmail || '—']].map(([label, val]) => (
                <div key={label}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 4 }}>{label}</div>
                  <div style={{ color: '#fff', fontSize: '0.9rem' }}>{val}</div>
                </div>
              ))}
            </div>
          </div>

          {/* ── Stage-aware checklist (new cases) ── */}
          {isNewStyleCase && (
            <div className="glass-card" style={{ marginTop: 20 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
                <h3 style={s.sectionTitle}>
                  {viewedStage ? `📄 ${viewedStage.name}` : '📋 Documents'}
                  {!isViewingCurrentStage && viewedStage && (
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 400, marginLeft: 8 }}>
                      {viewedStage.isCompleted ? '(Completed — read-only)' : '(Locked)'}
                    </span>
                  )}
                </h3>
                {isViewingCurrentStage && currentStage && (
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {currentChecklist.filter(c => c.documentId).length}/{currentChecklist.length} uploaded
                  </span>
                )}
              </div>

              {loadingViewedStage ? (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Loading stage documents…</p>
              ) : viewedChecklist.length === 0 ? (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>No document requirements for this stage.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                  {viewedChecklist.map(item => (
                    <StageUploadRow
                      key={item.requirementId}
                      item={item}
                      isExpanded={uploadingReqId === item.requirementId}
                      isSaving={savingReqId === item.requirementId}
                      onOpenUpload={() => setUploadingReqId(item.requirementId)}
                      onCancelUpload={() => setUploadingReqId(null)}
                      onSave={(file) => handleUpload(item.requirementId, file, true)}
                      onView={() => handleView(item.documentId!, getMimeFromFilename(item.originalFilename))}
                      readOnly={!isViewingCurrentStage}
                    />
                  ))}
                </div>
              )}

              {/* Mark Stage Complete */}
              {isViewingCurrentStage && currentStage && (
                <div style={{ marginTop: 20, paddingTop: 16, borderTop: '1px solid rgba(255,255,255,0.06)' }}>
                  <button
                    className="btn-primary"
                    style={{ width: '100%', opacity: allMandatoryUploaded ? 1 : 0.4, cursor: allMandatoryUploaded ? 'pointer' : 'not-allowed' }}
                    onClick={handleAdvanceStage}
                    disabled={!allMandatoryUploaded || advancing}
                  >
                    {advancing ? 'Advancing…' : hasMoreStages ? `✓ Mark Stage Complete — Advance to Next Stage →` : '✓ Complete All Stages'}
                  </button>
                  {!allMandatoryUploaded && (
                    <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: 8, textAlign: 'center' }}>
                      Upload all required documents above to enable stage completion.
                    </p>
                  )}
                </div>
              )}
            </div>
          )}

          {/* ── Legacy flat checklist (old cases) ── */}
          {!isNewStyleCase && (
            <div className="glass-card" style={{ marginTop: 20 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
                <h3 style={s.sectionTitle}>📋 Document Checklist</h3>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  {kase.checklistUploaded}/{kase.checklistTotal} uploaded · {legacyMandatoryUploaded}/{legacyMandatoryTotal} mandatory
                </span>
              </div>
              <div style={{ height: 6, background: 'rgba(255,255,255,0.06)', borderRadius: 3, marginBottom: 20 }}>
                <div style={{ height: '100%', width: `${progressPct}%`, borderRadius: 3, background: 'linear-gradient(90deg, var(--primary), var(--secondary))', transition: 'width 0.5s ease' }} />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {kase.checklist.map(item => (
                  <div key={item.requirementId} style={rowStyle}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span style={{ color: '#fff', fontSize: '0.875rem', fontWeight: 500 }}>{item.label}</span>
                        {item.mandatory && <span style={{ fontSize: '0.65rem', color: 'var(--color-warning)', border: '1px solid rgba(245,158,11,0.3)', borderRadius: 4, padding: '1px 6px', fontWeight: 600 }}>Required</span>}
                      </div>
                      {item.originalFilename && <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 3 }}>📎 {item.originalFilename}</p>}
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexShrink: 0 }}>
                      {(() => { const b = docBadge(item.documentStatus); return <span style={{ padding: '3px 10px', borderRadius: 9999, background: b.bg, color: b.color, fontSize: '0.72rem', fontWeight: 600 }}>{b.label}</span>; })()}
                      {item.documentId && <button className="btn-secondary" style={{ padding: '4px 10px', fontSize: '0.75rem' }} onClick={() => handleView(item.documentId!, getMimeFromFilename(item.originalFilename))}>👁 View</button>}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Notes thread */}
          <div className="glass-card" style={{ marginTop: 20 }}>
            <h3 style={s.sectionTitle}>💬 Case Notes</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 16 }}>
              {kase.notes.length === 0 && <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>No notes yet.</p>}
              {kase.notes.map(n => (
                <div key={n.id} style={{ background: 'rgba(255,255,255,0.02)', borderRadius: 10, padding: '12px 14px', border: '1px solid rgba(255,255,255,0.04)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                    <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--primary)' }}>{n.authorEmail}</span>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-dark)' }}>{new Date(n.createdAt).toLocaleString()}</span>
                  </div>
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-main)', lineHeight: 1.5 }}>{n.body}</p>
                </div>
              ))}
            </div>
            <form onSubmit={handleAddNote} style={{ display: 'flex', gap: 8 }}>
              <textarea className="form-input" style={{ flex: 1, minHeight: 60, resize: 'vertical' }} placeholder="Add a note…"
                value={noteBody} onChange={e => setNoteBody(e.target.value)} />
              <button type="submit" className="btn-primary" style={{ alignSelf: 'flex-end', padding: '10px 16px', fontSize: '0.85rem' }}
                disabled={addingNote || !noteBody.trim()}>
                {addingNote ? '…' : 'Add'}
              </button>
            </form>
          </div>
        </div>

        {/* RIGHT COLUMN */}
        <div style={s.rightCol}>
          {/* Status panel */}
          <div className="glass-card">
            <h3 style={s.sectionTitle}>📊 Case Status</h3>
            <div style={{ marginBottom: 20 }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 6 }}>Current Status</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 700, color: statusColor(kase.status) }}>{kase.status}</div>
            </div>
            {kase.allowedTransitions.length > 0 ? (
              <button className="btn-primary" style={{ width: '100%' }} onClick={() => {
                if (kase.allowedTransitions.length > 0) setNewStatus(kase.allowedTransitions[0]);
                setShowStatusModal(true);
              }}>Change Status →</button>
            ) : (
              <p style={{ fontSize: '0.8rem', color: 'var(--text-dark)' }}>No further transitions available.</p>
            )}
            <div style={{ marginTop: 24 }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 12 }}>History</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {kase.statusHistory.length === 0 && <p style={{ fontSize: '0.8rem', color: 'var(--text-dark)' }}>No history yet.</p>}
                {kase.statusHistory.map((h, i) => (
                  <div key={i} style={{ padding: '10px 12px', background: 'rgba(255,255,255,0.02)', borderRadius: 8, border: '1px solid rgba(255,255,255,0.04)' }}>
                    <div style={{ display: 'flex', gap: 6, alignItems: 'center', fontSize: '0.8rem' }}>
                      <span style={{ color: 'var(--text-muted)' }}>{h.fromStatus}</span>
                      <span style={{ color: 'var(--text-dark)' }}>→</span>
                      <span style={{ color: '#fff', fontWeight: 600 }}>{h.toStatus}</span>
                    </div>
                    {h.note && <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 4, fontStyle: 'italic' }}>"{h.note}"</p>}
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-dark)', marginTop: 4 }}>{h.changedBy} · {new Date(h.changedAt).toLocaleString()}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Case meta */}
          <div className="glass-card" style={{ marginTop: 16 }}>
            <h3 style={s.sectionTitle}>🗂 Case Info</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {[
                ['Reference', kase.caseReference],
                ['Visa Program', kase.visaProgramName || kase.visaTypeName || '—'],
                ...(kase.jobRoleCategory ? [['Job Role', kase.jobRoleCategory]] : []),
                ...(kase.currentStageName ? [['Current Stage', kase.currentStageName]] : []),
                ['Created By', kase.createdBy],
                ['Created', new Date(kase.createdAt).toLocaleDateString()],
              ].map(([label, val]) => (
                <div key={label} style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{label}</span>
                  <span style={{ fontSize: '0.8rem', color: '#fff', fontWeight: 500 }}>{val}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Status transition modal */}
      {showStatusModal && (
        <div style={s.overlay}>
          <div className="glass-card" style={s.modal}>
            <h3 style={{ color: '#fff', fontSize: '1.1rem', marginBottom: 20 }}>Change Case Status</h3>
            <form onSubmit={handleStatusTransition}>
              <div className="form-group">
                <label className="form-label">New Status</label>
                <select className="form-input" style={{ background: '#09090b', color: '#fff' }} value={newStatus} onChange={e => setNewStatus(e.target.value)}>
                  {kase.allowedTransitions.map(t => {
                    const friendlyLabels: Record<string, string> = {
                      DOCS_PENDING: 'Documents Pending', UNDER_REVIEW: 'Under Review', DRAFT: 'Draft',
                      SUBMITTED: 'Submitted', APPROVED: 'Approved', REJECTED: 'Rejected', CLOSED: 'Closed'
                    };
                    return <option key={t} value={t}>{friendlyLabels[t] ?? t}</option>;
                  })}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Note (optional)</label>
                <textarea className="form-input" style={{ minHeight: 80 }} placeholder="Describe the reason (optional)…" value={statusNote} onChange={e => setStatusNote(e.target.value)} />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 20 }}>
                <button type="button" className="btn-secondary" onClick={() => setShowStatusModal(false)} disabled={transitioning}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={transitioning}>{transitioning ? 'Saving…' : 'Confirm'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Document preview modal */}
      {previewUrl && (
        <div style={s.overlay} onClick={() => setPreviewUrl(null)}>
          <div
            style={{
              ...s.modal,
              maxWidth: '96vw',
              width: '96vw',
              maxHeight: '94vh',
              height: '92vh',
              padding: 0,
              display: 'flex',
              flexDirection: 'column',
              overflow: 'hidden',
              borderRadius: '16px',
            }}
            onClick={e => e.stopPropagation()}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 24px', borderBottom: '1px solid rgba(255,255,255,0.08)', flexShrink: 0, background: 'rgba(255,255,255,0.02)' }}>
              <h3 style={{ color: '#fff', fontSize: '1.05rem', fontWeight: 600 }}>Document Preview</h3>
              <button style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '1.3rem', cursor: 'pointer', padding: '2px 8px', borderRadius: 4 }} onClick={() => setPreviewUrl(null)}>✕</button>
            </div>
            <div style={{ flex: 1, width: '100%', height: '100%', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#09090b' }}>
              {previewMime.startsWith('image/') ? (
                <img src={previewUrl} alt="Document Preview" style={{ maxWidth: '100%', maxHeight: '100%', width: 'auto', height: 'auto', objectFit: 'contain' }} />
              ) : (
                <iframe src={previewUrl} style={{ width: '100%', height: '100%', border: 'none' }} title="Document Preview" />
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

const s: Record<string, React.CSSProperties> = {
  container: { display: 'flex', flexDirection: 'column', gap: 20 },
  headerRow: { display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' },
  errorAlert: { background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.2)', borderRadius: 10, color: 'var(--color-danger)', padding: '12px 16px', fontSize: '0.875rem' },
  grid: { display: 'grid', gridTemplateColumns: 'minmax(0,1.8fr) minmax(0,1.2fr)', gap: 24 },
  leftCol: { display: 'flex', flexDirection: 'column' },
  rightCol: { display: 'flex', flexDirection: 'column' },
  sectionTitle: { color: '#fff', fontSize: '1rem', fontWeight: 600, marginBottom: 16 },
  infoGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 },
  overlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 },
  modal: { width: '100%', maxWidth: 480, padding: 32, borderRadius: 20 },
};
