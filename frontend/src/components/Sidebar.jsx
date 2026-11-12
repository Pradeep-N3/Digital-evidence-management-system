import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Briefcase, FileCode2, Link2, ShieldAlert, Cpu } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Sidebar = () => {
  const { user } = useAuth();

  const links = [
    { to: '/', label: 'Overview', icon: LayoutDashboard },
    { to: '/cases', label: 'Investigation Cases', icon: Briefcase },
    { to: '/evidence', label: 'Evidence Vault', icon: FileCode2 },
    { to: '/custody', label: 'Custody Ledger', icon: Link2 },
  ];

  if (user && (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_AUDITOR')) {
    links.push({ to: '/audit', label: 'Audit & System Logs', icon: ShieldAlert });
  }

  return (
    <aside className="w-64 border-r border-slate-800 bg-[#0c101d] flex flex-col justify-between p-4 min-h-[calc(100vh-4rem)]">
      <div className="space-y-6">
        <div>
          <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider px-3 mb-2 font-mono">
            Navigation
          </div>
          <nav className="space-y-1">
            {links.map((link) => {
              const Icon = link.icon;
              return (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={link.to === '/'}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
                      isActive
                        ? 'bg-gradient-to-r from-cyan-500/20 to-blue-500/10 text-cyan-400 border border-cyan-500/30 shadow-sm'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                    }`
                  }
                >
                  <Icon className="w-4 h-4" />
                  <span>{link.label}</span>
                </NavLink>
              );
            })}
          </nav>
        </div>

        <div className="pt-4 border-t border-slate-800/80">
          <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider px-3 mb-2 font-mono">
            Security Status
          </div>
          <div className="bg-slate-900/60 border border-slate-800 p-3 rounded-xl space-y-2">
            <div className="flex items-center justify-between text-xs">
              <span className="text-slate-400 flex items-center gap-1.5">
                <Cpu className="w-3.5 h-3.5 text-cyan-400" /> Engine
              </span>
              <span className="font-mono text-cyan-400 font-semibold">SHA-256</span>
            </div>
            <div className="flex items-center justify-between text-xs">
              <span className="text-slate-400">Ledger</span>
              <span className="font-mono text-emerald-400 font-semibold">Chained &bull; Locked</span>
            </div>
            <div className="flex items-center justify-between text-xs">
              <span className="text-slate-400">Audit Rule</span>
              <span className="font-mono text-blue-400">Append-Only</span>
            </div>
          </div>
        </div>
      </div>

      <div className="p-3 rounded-xl bg-slate-900/40 border border-slate-800/60 text-[11px] text-slate-400 text-center font-mono">
        DECMS v1.0 • ISO/IEC 27037
      </div>
    </aside>
  );
};
