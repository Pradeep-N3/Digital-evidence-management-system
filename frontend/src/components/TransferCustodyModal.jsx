import React, { useState } from 'react';
import { X, ArrowRightLeft, ShieldCheck, AlertCircle } from 'lucide-react';
import api from '../api/client';

export const TransferCustodyModal = ({ isOpen, onClose, evidence, onCustodyTransferred }) => {
  const [transferredTo, setTransferredTo] = useState('');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen || !evidence) return null;

  const handleTransfer = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await api.post(`/evidence/${evidence.evidenceId}/transfer`, {
        transferredTo,
        notes,
      });
      onCustodyTransferred(response.data);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Custody transfer failed');
    } finally {
      setLoading(false);
    }
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
            <ArrowRightLeft className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">Record Custody Transfer</h2>
            <p className="text-xs text-slate-400">Appends a cryptographically hash-chained transfer block</p>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <div className="bg-slate-900/60 border border-slate-800 p-3 rounded-xl mb-4 text-xs space-y-1">
          <div className="flex justify-between">
            <span className="text-slate-400">Evidence ID:</span>
            <span className="font-mono text-cyan-400 font-semibold">{evidence.evidenceId}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-400">File Name:</span>
            <span className="text-slate-200 truncate max-w-[250px]">{evidence.fileName}</span>
          </div>
        </div>

        <form onSubmit={handleTransfer} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">
              Transferred To (Recipient Agency / Person) *
            </label>
            <input
              type="text"
              required
              value={transferredTo}
              onChange={(e) => setTransferredTo(e.target.value)}
              placeholder="e.g., Forensic Audio Lab Tech Marcus Vance (Badge #77)"
              className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">
              Transfer Purpose / Custody Handling Notes
            </label>
            <textarea
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Reason for transfer, handling precautions, evidentiary packaging seal verified..."
              className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500"
            />
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
              disabled={loading}
              className="px-5 py-2.5 rounded-xl text-sm font-semibold text-white bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 transition-all flex items-center gap-2 shadow-lg shadow-cyan-500/20 disabled:opacity-50"
            >
              <ShieldCheck className="w-4 h-4" />
              {loading ? 'Signing Block...' : 'Sign & Transfer Custody'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
