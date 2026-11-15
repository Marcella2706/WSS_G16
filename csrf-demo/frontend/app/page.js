"use client";

import { useEffect, useState } from "react";

const API = "http://localhost:8080";

// Fetch the anti-CSRF token from the backend. Returns {} when protection is OFF.
async function getCsrf() {
  const res = await fetch(`${API}/api/csrf`, { credentials: "include" });
  if (!res.ok) return {};
  return res.json();
}

export default function Home() {
  const [me, setMe] = useState(null);
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState({ u: "", p: "" });
  const [target, setTarget] = useState({ username: "", role: "USER" });
  const [err, setErr] = useState("");
  const [toast, setToast] = useState("");

  function flash(msg) {
    setToast(msg);
    setTimeout(() => setToast(""), 2600);
  }

  async function refresh() {
    setLoading(true);
    const res = await fetch(`${API}/api/me`, { credentials: "include" });
    if (res.status === 401) {
      setMe(null);
      setLoading(false);
      return;
    }
    const data = await res.json();
    setMe(data);
    if (data.role === "ADMIN") await loadUsers();
    setLoading(false);
  }

  async function loadUsers() {
    const res = await fetch(`${API}/api/admin/users`, { credentials: "include" });
    if (res.ok) {
      const list = await res.json();
      setUsers(list);
      if (list.length) setTarget((t) => ({ ...t, username: t.username || list[0].username }));
    }
  }

  async function login(e) {
    e.preventDefault();
    setErr("");
    const body = new URLSearchParams();
    body.set("username", form.u);
    body.set("password", form.p);
    const res = await fetch(`${API}/api/login`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body,
    });
    if (res.ok) refresh();
    else setErr("Invalid credentials.");
  }

  async function logout() {
    await fetch(`${API}/api/logout`, { method: "POST", credentials: "include" });
    setUsers([]);
    refresh();
  }

  async function updateRole() {
    const body = new URLSearchParams();
    body.set("username", target.username);
    body.set("role", target.role);
    const headers = { "Content-Type": "application/x-www-form-urlencoded" };
    // When CSRF protection is ON the backend hands us a token; echo it back.
    const csrf = await getCsrf();
    if (csrf.token) headers[csrf.headerName || "X-XSRF-TOKEN"] = csrf.token;
    const res = await fetch(`${API}/api/admin/update-role`, {
      method: "POST",
      credentials: "include",
      headers,
      body,
    });
    if (res.ok) {
      flash(`${target.username} is now ${target.role}`);
      await loadUsers();
      refresh();
    } else {
      flash(`Update failed (${res.status})`);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  return (
    <>
      <header>
        <div className="logo">
          Dev<span>Forge</span>
        </div>
        <div className="spacer" />
        {me && (
          <>
            <span className="muted">
              {me.username} · {me.role}
            </span>
            <button className="ghost" onClick={logout}>
              Sign out
            </button>
          </>
        )}
      </header>

      <div className="wrap">
        {loading ? (
          <div className="card muted">Loading…</div>
        ) : !me ? (
          <div className="card">
            <h2>Sign in to DevForge</h2>
            <form onSubmit={login}>
              <label>Username</label>
              <input
                value={form.u}
                onChange={(e) => setForm({ ...form, u: e.target.value })}
                placeholder="alice"
              />
              <label>Password</label>
              <input
                type="password"
                value={form.p}
                onChange={(e) => setForm({ ...form, p: e.target.value })}
                placeholder="••••••••"
              />
              <div style={{ marginTop: 16 }}>
                <button type="submit">Sign in</button>
              </div>
              {err && <div className="err">{err}</div>}
            </form>
            <div className="seed">
              <strong>Demo accounts</strong>
              <br />
              Admin — <code>alice</code> / <code>admin123</code>
              <br />
              Developer — <code>bob</code> / <code>user123</code>
              <br />
              Attacker — <code>mallory</code> / <code>hack123</code>
            </div>
          </div>
        ) : (
          <>
            <div className="card">
              <h2>Your profile</h2>
              <table>
                <tbody>
                  <tr>
                    <th>User</th>
                    <td>{me.username}</td>
                  </tr>
                  <tr>
                    <th>Role</th>
                    <td>
                      <span className={`badge ${me.role.toLowerCase()}`}>{me.role}</span>
                    </td>
                  </tr>
                  <tr>
                    <th>Email</th>
                    <td>{me.email}</td>
                  </tr>
                  <tr>
                    <th>CSRF protection</th>
                    <td>
                      {me.csrfProtection ? (
                        <span className="pill on">ENABLED</span>
                      ) : (
                        <span className="pill off">DISABLED (vulnerable)</span>
                      )}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            {me.role === "ADMIN" ? (
              <div className="card">
                <h2>Team administration</h2>
                <p className="muted">Manage developer access levels for the DevForge org.</p>
                <table>
                  <thead>
                    <tr>
                      <th>Username</th>
                      <th>Email</th>
                      <th>Role</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((u) => (
                      <tr key={u.username}>
                        <td>{u.username}</td>
                        <td>{u.email}</td>
                        <td>
                          <span className={`badge ${u.role.toLowerCase()}`}>{u.role}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <hr />
                <h2 style={{ fontSize: 14 }}>Update a member&apos;s role</h2>
                <div className="row">
                  <div>
                    <label>Member</label>
                    <select
                      value={target.username}
                      onChange={(e) => setTarget({ ...target, username: e.target.value })}
                    >
                      {users.map((u) => (
                        <option key={u.username} value={u.username}>
                          {u.username}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label>New role</label>
                    <select
                      value={target.role}
                      onChange={(e) => setTarget({ ...target, role: e.target.value })}
                    >
                      <option value="USER">USER</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  </div>
                  <div style={{ flex: "0 0 auto" }}>
                    <button onClick={updateRole}>Apply</button>
                  </div>
                </div>
                <div className="muted" style={{ marginTop: 8 }}>
                  Issues a state-changing <code>POST /api/admin/update-role</code>.
                </div>
              </div>
            ) : (
              <div className="card muted">
                You are signed in as a standard developer. The team administration panel is
                only visible to admins.
              </div>
            )}
          </>
        )}
      </div>

      {toast && <div className="toast">{toast}</div>}
    </>
  );
}
