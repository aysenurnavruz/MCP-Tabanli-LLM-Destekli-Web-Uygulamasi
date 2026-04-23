import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

function RegisterPage() {
  const navigate = useNavigate();
  const { register, loading, error } = useAuth();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const ok = await register(form);
    if (ok) navigate("/chat");
  }

  return (
    <div className="min-h-screen grid place-items-center bg-neutral-950 text-white px-4">
      <form
        onSubmit={onSubmit}
        className="w-full max-w-md rounded-2xl border border-white/10 bg-white/5 p-6"
      >
        <h1 className="text-2xl font-semibold">Kayıt Ol</h1>

        <div className="mt-6 space-y-3">
          <input
            className="w-full rounded-lg border border-white/15 bg-black/30 px-3 py-2"
            placeholder="E-posta"
            type="email"
            value={form.email}
            onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))}
            required
          />
          <input
            type="password"
            className="w-full rounded-lg border border-white/15 bg-black/30 px-3 py-2"
            placeholder="Şifre (min 6)"
            minLength={6}
            value={form.password}
            onChange={(e) => setForm((p) => ({ ...p, password: e.target.value }))}
            required
          />
          <button
            disabled={loading}
            className="w-full rounded-lg bg-white text-black py-2 font-medium disabled:opacity-60"
          >
            {loading ? "Kayıt olunuyor..." : "Kayıt Ol"}
          </button>
        </div>

        {error ? <p className="mt-3 text-sm text-red-400">{error}</p> : null}

        <p className="mt-4 text-sm text-white/70">
          Hesabın var mı?{" "}
          <Link to="/login" className="text-white underline">
            Giriş yap
          </Link>
        </p>
      </form>
    </div>
  );
}

export default RegisterPage;