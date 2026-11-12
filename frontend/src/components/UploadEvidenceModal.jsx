import React, { useState } from 'react';
import { X, UploadCloud, FileCode2, Check, AlertCircle } from 'lucide-react';
import api from '../api/client';

export const UploadEvidenceModal = ({ isOpen, onClose, preselectedCaseId, cases = [], onEvidenceUploaded }) => {
  const [selectedCaseId, setSelectedCaseId] = useState(preselectedCaseId || (cases[0]?.caseId || ''));
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) {
      setError('Please select an evidence file to upload');
      return;
    }
    const targetCaseId = preselectedCaseId || selectedCaseId;
    if (!targetCaseId) {
      setError('Please select an investigation case');
      return;
    }

    setUploading(true);
    setError('');

    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await api.post(`/cases/${targetCaseId}/evidence`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      onEvidenceUploaded(response.data);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to ingest evidence file');
    } finally {
      setUploading(false);
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
      <div className="bg-[#101726] border border-slate-800 rounded-2xl max-w-lg w-full p-6 shadow-2xl relative">
        <button
          onClick={onClose}
          className="absolute top-5 right-5 text-slate-400 hover:text-slate-200"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-6">
          <div className="p-2.5 rounded-xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400">
            <FileCode2 className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">Ingest Digital Evidence</h2>
            <p className="text-xs text-slate-400">Calculates SHA-256 hash & initializes immutable custody chain</p>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleUpload} className="space-y-4">
          {!preselectedCaseId && (
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">
                Target Investigation Case *
              </label>
              <select
                value={selectedCaseId}
                onChange={(e) => setSelectedCaseId(e.target.value)}
                className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-cyan-500"
              >
                {cases.map((c) => (
                  <option key={c.caseId} value={c.caseId}>
                    {c.caseId} - {c.title}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">
              Select Evidence Artifact *
            </label>
            <div className="border-2 border-dashed border-slate-700 hover:border-cyan-500/50 rounded-2xl p-6 text-center bg-slate-900/50 transition-colors">
              <input
                type="file"
                id="evidence-file"
                onChange={handleFileChange}
                className="hidden"
              />
              <label htmlFor="evidence-file" className="cursor-pointer block">
                <UploadCloud className="w-10 h-10 text-cyan-400 mx-auto mb-2 opacity-80" />
                <span className="text-sm font-medium text-slate-200 block">
                  {file ? file.name : 'Click to select or drop evidence file here'}
                </span>
                <span className="text-xs text-slate-400 mt-1 block">
                  {file ? `${formatFileSize(file.size)} • ${file.type || 'Unknown MIME'}` : 'Supports PCAP, PDF, Images, Videos, Disk Logs, Text'}
                </span>
              </label>
            </div>
          </div>

          <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 text-[11px] text-slate-400 space-y-1">
            <div className="flex items-center gap-1.5 text-cyan-400 font-semibold">
              <Check className="w-3.5 h-3.5" /> Cryptographic Intake Protocol
            </div>
            <p>Upon submission, backend streams file through SHA-256 MessageDigest, assigns unique identifier (EVD-YYYY-XXXX), extracts metadata, and creates Block #0 in the custody ledger.</p>
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-sm font-medium text-slate-400 hover:text-slate-200 hover:bg-slate-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={uploading || !file}
              className="px-5 py-2.5 rounded-xl text-sm font-semibold text-white bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 transition-all flex items-center gap-2 shadow-lg shadow-cyan-500/20 disabled:opacity-50"
            >
              <UploadCloud className="w-4 h-4" />
              {uploading ? 'Hashing & Ingesting...' : 'Upload & Seal Evidence'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
