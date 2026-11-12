import React from 'react';
import { Link2, ShieldCheck, ShieldAlert, ArrowRight, User, Calendar, FileText } from 'lucide-react';

export const CustodyTimeline = ({ records = [] }) => {
  if (!records || records.length === 0) {
    return (
      <div className="p-8 text-center text-slate-500 bg-slate-900/30 rounded-2xl border border-slate-800">
        No custody ledger records found for this evidence item.
      </div>
    );
  }

  const getActionColor = (action) => {
    switch (action) {
      case 'UPLOADED':
        return 'text-blue-400 bg-blue-500/10 border-blue-500/30';
      case 'VERIFIED':
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
      case 'TRANSFERRED':
        return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
      case 'DOWNLOADED':
        return 'text-purple-400 bg-purple-500/10 border-purple-500/30';
      default:
        return 'text-cyan-400 bg-cyan-500/10 border-cyan-500/30';
    }
  };

  return (
    <div className="relative pl-6 space-y-6 before:absolute before:left-2.5 before:top-3 before:bottom-3 before:w-0.5 before:bg-gradient-to-b before:from-cyan-500 before:via-blue-500 before:to-purple-500">
      {records.map((record, index) => {
        const isGenesis = index === 0;
        return (
          <div key={record.id || index} className="relative group">
            {/* Timeline node icon */}
            <div className={`absolute -left-[27px] top-1.5 w-6 h-6 rounded-full border flex items-center justify-center bg-[#0c101d] ${
              record.blockIntegrityValid ? 'border-cyan-500 text-cyan-400' : 'border-rose-500 text-rose-400'
            }`}>
              {record.blockIntegrityValid ? <ShieldCheck className="w-3.5 h-3.5" /> : <ShieldAlert className="w-3.5 h-3.5" />}
            </div>

            <div className="bg-slate-900/70 border border-slate-800 hover:border-slate-700 transition-all rounded-2xl p-4.5 space-y-3 shadow-md">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <span className={`px-2.5 py-0.5 rounded-lg text-xs font-bold border ${getActionColor(record.action)}`}>
                    {record.action}
                  </span>
                  <span className="text-xs font-mono font-semibold text-slate-400">
                    {isGenesis ? 'BLOCK #0 (GENESIS)' : `BLOCK #${index}`}
                  </span>
                </div>

                <div className="flex items-center gap-3 text-xs text-slate-400 font-mono">
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    {new Date(record.timestamp).toLocaleString()}
                  </span>
                  {record.blockIntegrityValid ? (
                    <span className="text-emerald-400 flex items-center gap-1 font-sans font-semibold text-[11px]">
                      <ShieldCheck className="w-3 h-3" /> Block Seal Valid
                    </span>
                  ) : (
                    <span className="text-rose-400 flex items-center gap-1 font-sans font-semibold text-[11px]">
                      <ShieldAlert className="w-3 h-3" /> Block Tampered
                    </span>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
                <div className="flex items-center gap-2 text-slate-300">
                  <User className="w-3.5 h-3.5 text-cyan-400 shrink-0" />
                  <span className="text-slate-400">Custodian:</span>
                  <span className="font-medium text-slate-200">{record.performedByFullName || record.performedByUsername}</span>
                </div>

                {record.transferredTo && (
                  <div className="flex items-center gap-2 text-slate-300">
                    <ArrowRight className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                    <span className="text-slate-400">Transferred To:</span>
                    <span className="font-semibold text-amber-300">{record.transferredTo}</span>
                  </div>
                )}
              </div>

              {record.notes && (
                <div className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800/80 text-xs text-slate-300 flex items-start gap-2">
                  <FileText className="w-3.5 h-3.5 text-slate-400 shrink-0 mt-0.5" />
                  <span>{record.notes}</span>
                </div>
              )}

              {/* Cryptographic Hash Chaining Block details */}
              <div className="pt-2 border-t border-slate-800/60 font-mono text-[11px] space-y-1">
                <div className="flex items-center justify-between text-slate-400">
                  <span className="flex items-center gap-1">
                    <Link2 className="w-3 h-3 text-cyan-400" /> Previous Block Pointer:
                  </span>
                  <span className="text-slate-300 truncate max-w-[340px]" title={record.previousRecordHash}>
                    {record.previousRecordHash}
                  </span>
                </div>
                <div className="flex items-center justify-between text-slate-400">
                  <span className="flex items-center gap-1">
                    <ShieldCheck className="w-3 h-3 text-emerald-400" /> Current Block Checksum:
                  </span>
                  <span className="text-cyan-400 font-semibold truncate max-w-[340px]" title={record.recordHash}>
                    {record.recordHash}
                  </span>
                </div>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};
