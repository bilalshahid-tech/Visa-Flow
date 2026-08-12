'use client';

import React, { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { apiFetch } from '@/services/api';

interface ChecklistItem {
  requirementId: string;
  documentClass: string;
  label: string;
  mandatory: boolean;
  displayOrder: number;
  documentId: string | null;
  documentStatus: string | null;
  originalFilename: string | null;
  reviewerNotes: string | null;
}

interface HistoryItem { fromStatus: string; toStatus: string; changedBy: string; note: string; changedAt: string; }
interface NoteItem { id: string; authorId: string; authorEmail: string; body: string; createdAt: string; }

interface CaseDetail {
  id: string; caseReference: string; status: string;
  allowedTransitions: string[];
  clientName: string; clientPassportNumber: string; clientNationality: string;
  clientDateOfBirth: string; clientPhone: string; clientEmail: string;
  visaTypeName: string; visaTypeCode: string;
  checklist: ChecklistItem[];
  checklistTotal: number; checklistUploaded: number;
  statusHistory: HistoryItem[];
  notes: NoteItem[];
  createdAt: string; createdBy: string;
}

export default function CaseDetailsPage() {
  const { id } = useParams() as { id: string };
  const [kase, setKase] = useState<CaseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Status transition state
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [newStatus, setNewStatus] = useState('');
  const [statusNote, setStatusNote] = useState('');
  const [transitioning, setTransitioning] = useState(false);

  // Note state
  const [noteBody, setNoteBody] = useState('');
  const [addingNote, setAddingNote] = useState(false);

  // Upload state
  const [uploadingReqId, setUploadingReqId] = useState<string | null>(null);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploadingId, setUploadingId] = useState('');

  // Document preview modal
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [previewMime, setPreviewMime] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const d = await apiFetch<CaseDetail>(`/cases/${id}`);
      setKase(d);
      if (d.allowedTransitions.length > 0) setNewStatus(d.allowedTransitions[0]);
    } catch (e: any) { setError(e.message || 'Failed to load case.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { if (id) load(); }, [id]);

  const handleStatusTransition = async (e: React.FormEvent) => {
    e.preventDefault();
    setTransitioning(true);
    try {
      // find enum name from friendly name
      const raw = rawStatus(newStatus);
      await apiFetch(`/cases/${id}/status`, { method:'PATCH', bodyData: { newStatus: raw, note: statusNote } });
      setShowStatusModal(false); setStatusNote('');
      await load();
    } catch (e: any) { setError(e.message || 'Transition failed.'); }
    finally { setTransitioning(false); }
  };

  const handleAddNote = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!noteBody.trim()) return;
    setAddingNote(true);
    try {
      await apiFetch(`/cases/${id}/notes`, { method:'POST', bodyData: { body: noteBody } });
      setNoteBody(''); await load();
    } catch (e: any) { setError(e.message || 'Could not add note.'); }
    finally { setAddingNote(false); }
  };

  const handleUpload = async (reqId: string) => {
    if (!uploadFile) return;
    setUploadingId(reqId);
    const fd = new FormData();
    fd.append('file', uploadFile);
    if (reqId !== 'adhoc') fd.append('requirementId', reqId);
    try {
      await apiFetch(`/cases/${id}/documents`, { method:'POST', body: fd });
      setUploadingReqId(null); setUploadFile(null); await load();
    } catch (e: any) { setError(e.message || 'Upload failed.'); }
    finally { setUploadingId(''); }
  };

  const handleView = async (docId: string, mime: string) => {
    try {
      const url = await apiFetch<string>(`/cases/${id}/documents/${docId}/view`);
      setPreviewUrl(url); setPreviewMime(mime || 'application/pdf');
    } catch (e: any) { setError(e.message || 'Could not load document.'); }
  };

  // Converts friendly label back to enum for API
  const rawStatus = (friendly: string) => {
    const map: Record<string,string> = {
      'Documents Pending':'DOCS_PENDING','Under Review':'UNDER_REVIEW','Draft':'DRAFT',
      'Submitted':'SUBMITTED','Approved':'APPROVED','Rejected':'REJECTED','Closed':'CLOSED'
    };
    return map[friendly] || friendly;
  };

  const statusColor = (s?: string | null) => {
    if (!s) return 'var(--text-muted)';
    if (s.toLowerCase().includes('approv')) return 'var(--color-success)';
    if (s.toLowerCase().includes('reject')) return 'var(--color-danger)';
    if (s.toLowerCase().includes('pending') || s.toLowerCase().includes('review')) return 'var(--color-warning)';
    if (s.toLowerCase().includes('draft')) return 'var(--text-dark)';
    if (s.toLowerCase().includes('closed')) return '#7c3aed';
    return 'var(--primary)';
  };

  const docStatusBadge = (status: string | null) => {
    if (!status) return { label: 'Missing', bg: 'rgba(239,68,68,0.1)', color: 'var(--color-danger)' };
    if (status === 'APPROVED') return { label: 'Approved', bg: 'rgba(16,185,129,0.1)', color: 'var(--color-success)' };
    if (status === 'REJECTED') return { label: 'Rejected', bg: 'rgba(239,68,68,0.1)', color: 'var(--color-danger)' };
    return { label: 'Pending Review', bg: 'rgba(245,158,11,0.1)', color: 'var(--color-warning)' };
  };

  if (loading) return <div style={{ color:'var(--text-muted)', padding:'60px', textAlign:'center' }}>Loading case…</div>;
  if (!kase) return <div style={{ padding:'40px', textAlign:'center' }}><p style={{ color:'var(--color-danger)' }}>Case not found.</p><Link href="/dashboard/cases" className="btn-secondary" style={{ marginTop:16, display:'inline-block' }}>← Cases</Link></div>;

  const mandatoryUploaded = kase.checklist.filter(c => c.mandatory && c.documentId).length;
  const mandatoryTotal = kase.checklist.filter(c => c.mandatory).length;
  const progressPct = kase.checklistTotal > 0 ? Math.round((kase.checklistUploaded / kase.checklistTotal) * 100) : 0;

  return (
    <div style={s.container}>
      {/* Header */}
      <div style={s.headerRow}>
        <Link href="/dashboard/cases" className="btn-secondary" style={{ padding:'8px 16px', fontSize:'0.85rem', textDecoration:'none' }}>← Cases</Link>
        <div>
          <h2 style={{ color:'#fff', fontSize:'1.4rem' }}>{kase.caseReference}</h2>
          <p style={{ color:'var(--text-muted)', fontSize:'0.8rem', marginTop:2 }}>{kase.visaTypeName}</p>
        </div>
        <span style={{ padding:'6px 16px', borderRadius:9999, background:'rgba(99,102,241,0.1)', color: statusColor(kase.status), fontWeight:700, fontSize:'0.85rem', border:`1px solid ${statusColor(kase.status)}40` }}>
          {kase.status}
        </span>
      </div>

      {error && <div style={s.errorAlert}>{error}<button style={{ float:'right', background:'none', border:'none', color:'inherit', cursor:'pointer' }} onClick={() => setError('')}>✕</button></div>}

      <div style={s.grid}>
        {/* LEFT COLUMN */}
        <div style={s.leftCol}>

          {/* Client info */}
          <div className="glass-card">
            <h3 style={s.sectionTitle}>👤 Client Information</h3>
            <div style={s.infoGrid}>
              {[['Full Name', kase.clientName],['Passport', kase.clientPassportNumber],['Nationality', kase.clientNationality],['Date of Birth', kase.clientDateOfBirth],['Phone', kase.clientPhone || '—'],['Email', kase.clientEmail || '—']].map(([label, val]) => (
                <div key={label}>
                  <div style={{ fontSize:'0.75rem', color:'var(--text-muted)', textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:4 }}>{label}</div>
                  <div style={{ color:'#fff', fontSize:'0.9rem' }}>{val}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Document checklist */}
          <div className="glass-card" style={{ marginTop:20 }}>
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:16 }}>
              <h3 style={s.sectionTitle}>📋 Document Checklist</h3>
              <span style={{ fontSize:'0.8rem', color:'var(--text-muted)' }}>
                {kase.checklistUploaded}/{kase.checklistTotal} uploaded · {mandatoryUploaded}/{mandatoryTotal} mandatory
              </span>
            </div>

            {/* Progress bar */}
            <div style={{ height:6, background:'rgba(255,255,255,0.06)', borderRadius:3, marginBottom:20 }}>
              <div style={{ height:'100%', width:`${progressPct}%`, borderRadius:3, background:'linear-gradient(90deg, var(--primary), var(--secondary))', transition:'width 0.5s ease' }}/>
            </div>

            <div style={{ display:'flex', flexDirection:'column', gap:12 }}>
              {kase.checklist.map(item => {
                const badge = docStatusBadge(item.documentStatus);
                const isUploading = uploadingReqId === item.requirementId;
                return (
                  <div key={item.requirementId} style={s.checklistRow}>
                    <div style={{ flex:1 }}>
                      <div style={{ display:'flex', alignItems:'center', gap:8 }}>
                        <span style={{ color:'#fff', fontSize:'0.875rem', fontWeight:500 }}>{item.label}</span>
                        {item.mandatory && <span style={{ fontSize:'0.65rem', color:'var(--color-warning)', border:'1px solid rgba(245,158,11,0.3)', borderRadius:4, padding:'1px 6px', fontWeight:600 }}>Required</span>}
                      </div>
                      {item.originalFilename && <p style={{ fontSize:'0.75rem', color:'var(--text-muted)', marginTop:3 }}>📎 {item.originalFilename}</p>}
                      {item.reviewerNotes && <p style={{ fontSize:'0.75rem', color:'var(--color-danger)', marginTop:3, fontStyle:'italic' }}>🔴 {item.reviewerNotes}</p>}
                    </div>

                    <div style={{ display:'flex', alignItems:'center', gap:8, flexShrink:0 }}>
                      <span style={{ padding:'3px 10px', borderRadius:9999, background:badge.bg, color:badge.color, fontSize:'0.72rem', fontWeight:600 }}>{badge.label}</span>

                      {item.documentId && (
                        <button className="btn-secondary" style={{ padding:'4px 10px', fontSize:'0.75rem' }}
                          onClick={() => handleView(item.documentId!, item.documentId ? 'application/pdf' : '')}>
                          👁 View
                        </button>
                      )}

                      {!isUploading ? (
                        <button className="btn-secondary" style={{ padding:'4px 10px', fontSize:'0.75rem' }}
                          onClick={() => { setUploadingReqId(item.requirementId); setUploadFile(null); }}>
                          ↑ Upload
                        </button>
                      ) : (
                        <div style={{ display:'flex', gap:6, alignItems:'center' }}>
                          <input type="file" accept="image/*,application/pdf" style={{ fontSize:'0.75rem', maxWidth:150, color:'var(--text-muted)' }}
                            onChange={e => setUploadFile(e.target.files?.[0] ?? null)} />
                          <button className="btn-primary" style={{ padding:'4px 10px', fontSize:'0.75rem' }}
                            onClick={() => handleUpload(item.requirementId)} disabled={!uploadFile || uploadingId === item.requirementId}>
                            {uploadingId === item.requirementId ? '…' : 'Save'}
                          </button>
                          <button className="btn-secondary" style={{ padding:'4px 10px', fontSize:'0.75rem' }}
                            onClick={() => setUploadingReqId(null)}>✕</button>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Notes thread */}
          <div className="glass-card" style={{ marginTop:20 }}>
            <h3 style={s.sectionTitle}>💬 Case Notes</h3>
            <div style={{ display:'flex', flexDirection:'column', gap:10, marginBottom:16 }}>
              {kase.notes.length === 0 && <p style={{ color:'var(--text-muted)', fontSize:'0.875rem' }}>No notes yet.</p>}
              {kase.notes.map(n => (
                <div key={n.id} style={{ background:'rgba(255,255,255,0.02)', borderRadius:10, padding:'12px 14px', border:'1px solid rgba(255,255,255,0.04)' }}>
                  <div style={{ display:'flex', justifyContent:'space-between', marginBottom:6 }}>
                    <span style={{ fontSize:'0.8rem', fontWeight:600, color:'var(--primary)' }}>{n.authorEmail}</span>
                    <span style={{ fontSize:'0.72rem', color:'var(--text-dark)' }}>{new Date(n.createdAt).toLocaleString()}</span>
                  </div>
                  <p style={{ fontSize:'0.875rem', color:'var(--text-main)', lineHeight:1.5 }}>{n.body}</p>
                </div>
              ))}
            </div>
            <form onSubmit={handleAddNote} style={{ display:'flex', gap:8 }}>
              <textarea className="form-input" style={{ flex:1, minHeight:60, resize:'vertical' }} placeholder="Add a note…"
                value={noteBody} onChange={e => setNoteBody(e.target.value)} />
              <button type="submit" className="btn-primary" style={{ alignSelf:'flex-end', padding:'10px 16px', fontSize:'0.85rem' }} disabled={addingNote || !noteBody.trim()}>
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

            <div style={{ marginBottom:20 }}>
              <div style={{ fontSize:'0.75rem', color:'var(--text-muted)', marginBottom:6 }}>Current Status</div>
              <div style={{ fontSize:'1.25rem', fontWeight:700, color: statusColor(kase.status) }}>{kase.status}</div>
            </div>

            {kase.allowedTransitions.length > 0 ? (
              <button className="btn-primary" style={{ width:'100%' }} onClick={() => setShowStatusModal(true)}>
                Change Status →
              </button>
            ) : (
              <p style={{ fontSize:'0.8rem', color:'var(--text-dark)' }}>No further transitions available.</p>
            )}

            {/* Status history */}
            <div style={{ marginTop:24 }}>
              <div style={{ fontSize:'0.8rem', color:'var(--text-muted)', textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:12 }}>History</div>
              <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
                {kase.statusHistory.length === 0 && <p style={{ fontSize:'0.8rem', color:'var(--text-dark)' }}>No history yet.</p>}
                {kase.statusHistory.map((h, i) => (
                  <div key={i} style={{ padding:'10px 12px', background:'rgba(255,255,255,0.02)', borderRadius:8, border:'1px solid rgba(255,255,255,0.04)' }}>
                    <div style={{ display:'flex', gap:6, alignItems:'center', fontSize:'0.8rem' }}>
                      <span style={{ color:'var(--text-muted)' }}>{h.fromStatus}</span>
                      <span style={{ color:'var(--text-dark)' }}>→</span>
                      <span style={{ color:'#fff', fontWeight:600 }}>{h.toStatus}</span>
                    </div>
                    {h.note && <p style={{ fontSize:'0.75rem', color:'var(--text-muted)', marginTop:4, fontStyle:'italic' }}>"{h.note}"</p>}
                    <div style={{ fontSize:'0.7rem', color:'var(--text-dark)', marginTop:4 }}>{h.changedBy} · {new Date(h.changedAt).toLocaleString()}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Case meta */}
          <div className="glass-card" style={{ marginTop:16 }}>
            <h3 style={s.sectionTitle}>🗂 Case Info</h3>
            <div style={{ display:'flex', flexDirection:'column', gap:12 }}>
              {[['Reference', kase.caseReference],['Visa Type', kase.visaTypeName],['Created By', kase.createdBy],['Created', new Date(kase.createdAt).toLocaleDateString()]].map(([label, val]) => (
                <div key={label} style={{ display:'flex', justifyContent:'space-between' }}>
                  <span style={{ fontSize:'0.8rem', color:'var(--text-muted)' }}>{label}</span>
                  <span style={{ fontSize:'0.8rem', color:'#fff', fontWeight:500 }}>{val}</span>
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
            <h3 style={{ color:'#fff', fontSize:'1.1rem', marginBottom:20 }}>Change Case Status</h3>
            <form onSubmit={handleStatusTransition}>
              <div className="form-group">
                <label className="form-label">New Status</label>
                <select className="form-input" style={{ background:'#09090b', color:'#fff' }} value={newStatus} onChange={e => setNewStatus(e.target.value)}>
                  {kase.allowedTransitions.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Note (required)</label>
                <textarea className="form-input" style={{ minHeight:80 }} placeholder="Describe the reason for this change…"
                  value={statusNote} onChange={e => setStatusNote(e.target.value)} required />
              </div>
              <div style={{ display:'flex', justifyContent:'flex-end', gap:10, marginTop:20 }}>
                <button type="button" className="btn-secondary" onClick={() => setShowStatusModal(false)} disabled={transitioning}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={transitioning || !statusNote.trim()}>{transitioning ? 'Saving…' : 'Confirm'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Document preview modal */}
      {previewUrl && (
        <div style={s.overlay} onClick={() => setPreviewUrl(null)}>
          <div style={{ ...s.modal, maxWidth:820, width:'90vw', maxHeight:'85vh', padding:0, overflow:'hidden' }} onClick={e => e.stopPropagation()}>
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', padding:'16px 20px', borderBottom:'1px solid rgba(255,255,255,0.06)' }}>
              <h3 style={{ color:'#fff', fontSize:'1rem' }}>Document Preview</h3>
              <button style={{ background:'none', border:'none', color:'var(--text-muted)', fontSize:'1.2rem', cursor:'pointer' }} onClick={() => setPreviewUrl(null)}>✕</button>
            </div>
            <div style={{ height:'70vh', overflow:'hidden' }}>
              {previewMime.startsWith('image/') ? (
                <img src={previewUrl} alt="Document" style={{ width:'100%', height:'100%', objectFit:'contain' }} />
              ) : (
                <iframe src={previewUrl} style={{ width:'100%', height:'100%', border:'none' }} title="Document Preview" />
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

const s: Record<string, React.CSSProperties> = {
  container: { display:'flex', flexDirection:'column', gap:20 },
  headerRow: { display:'flex', alignItems:'center', gap:16, flexWrap:'wrap' },
  errorAlert: { background:'rgba(239,68,68,0.1)', border:'1px solid rgba(239,68,68,0.2)', borderRadius:10, color:'var(--color-danger)', padding:'12px 16px', fontSize:'0.875rem' },
  grid: { display:'grid', gridTemplateColumns:'minmax(0,1.8fr) minmax(0,1.2fr)', gap:24 },
  leftCol: { display:'flex', flexDirection:'column' },
  rightCol: { display:'flex', flexDirection:'column' },
  sectionTitle: { color:'#fff', fontSize:'1rem', fontWeight:600, marginBottom:16 },
  infoGrid: { display:'grid', gridTemplateColumns:'1fr 1fr', gap:16 },
  checklistRow: { display:'flex', alignItems:'flex-start', justifyContent:'space-between', padding:'12px 14px', background:'rgba(255,255,255,0.02)', borderRadius:10, border:'1px solid rgba(255,255,255,0.04)', gap:12 },
  overlay: { position:'fixed', top:0, left:0, right:0, bottom:0, background:'rgba(0,0,0,0.7)', backdropFilter:'blur(8px)', display:'flex', alignItems:'center', justifyContent:'center', zIndex:200 },
  modal: { width:'100%', maxWidth:480, padding:32, borderRadius:20 },
};
