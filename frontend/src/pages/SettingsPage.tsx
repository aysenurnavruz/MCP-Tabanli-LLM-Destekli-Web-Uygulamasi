import { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { changePassword, getCurrentUserEmail } from "@/api/profile";
import { useTheme } from "@/hooks/useTheme";

function parseError(err: unknown) {
  const e = err as { response?: { status?: number; data?: { message?: string } }; message?: string };
  if (e?.response?.status === 404) {
    return "Parola guncelleme endpoint'i backendde henuz yok.";
  }
  return e?.response?.data?.message || e?.message || "Islem basarisiz oldu.";
}

function SettingsPage() {
  const { theme, setTheme } = useTheme();
  const [email, setEmail] = useState("");
  const [showPasswordForm, setShowPasswordForm] = useState(false);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loadingUser, setLoadingUser] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    const loadUser = async () => {
      setLoadingUser(true);
      setError("");
      try {
        const currentUserEmail = await getCurrentUserEmail();
        setEmail(currentUserEmail);
      } catch (err) {
        setError(parseError(err));
      } finally {
        setLoadingUser(false);
      }
    };

    loadUser();
  }, []);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!currentPassword || !newPassword || !confirmPassword) {
      setError("Tum alanlari doldurman gerekiyor.");
      return;
    }

    if (newPassword.length < 6) {
      setError("Yeni parola en az 6 karakter olmali.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setError("Yeni parola ve tekrar parola ayni olmali.");
      return;
    }

    setSaving(true);
    try {
      await changePassword({
        currentPassword,
        newPassword,
      });
      setSuccess("Parolan basariyla guncellendi.");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setError(parseError(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-svh flex flex-col bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100">
    

        <div className="px-4 py-8">
          <div className="mx-auto w-full max-w-xl rounded-2xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 p-6">
        <h1 className="text-2xl font-semibold">Ayarlar</h1>
        <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
          Hesap ayarlarını buradan yönetebilirsin.
        </p>

        <div className="mt-6 space-y-2">
          <p className="text-xs text-zinc-400">Kullanıcı</p>
          {loadingUser ? (
            <p className="text-sm text-zinc-300 inline-flex items-center gap-2">
              <Loader2 className="size-4 animate-spin" />
              Kullanıcı bilgisi yükleniyor
            </p>
          ) : (
            <p className="text-sm dark:text-zinc-100 hover:bg-zinc-100 dark:hover:bg-zinc-800">{email || "-"}</p>
          )}
        </div>

        <div className="mt-6">
          <Button
            type="button"
            variant="outline"
            onClick={() => setShowPasswordForm((prev) => !prev)}
            className="border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100 hover:bg-zinc-100 dark:hover:bg-zinc-800"
          >
            Parola Degiştir
          </Button>
        </div>

        <div className="mt-6 space-y-3">
          <p className="text-xs text-zinc-400">Tema</p>
          <Select value={theme} onValueChange={(value: "light" | "dark" | "system") => setTheme(value)}>
            <SelectTrigger className="w-48 border-zinc-300 bg-white text-zinc-900 dark:border-zinc-700 dark:bg-zinc-950 dark:text-zinc-100">
              <SelectValue />
            </SelectTrigger>
            <SelectContent className="border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100">
              <SelectItem value="light">Açık</SelectItem>
              <SelectItem value="dark">Koyu </SelectItem>
              <SelectItem value="system">Sistem </SelectItem>
            </SelectContent>
          </Select>
        </div>

        {showPasswordForm ? (
          <form onSubmit={onSubmit} className="mt-6 space-y-4">
            <div className="space-y-2">
              <label className="text-sm text-zinc-300">Mevcut Parola</label>
              <Input
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                placeholder="Mevcut parolani gir"
                className="bg-white dark:bg-zinc-950 border-zinc-300 dark:border-zinc-800"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm text-zinc-300">Yeni Parola</label>
              <Input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Yeni parolani gir"
                className="bg-white dark:bg-zinc-950 border-zinc-300 dark:border-zinc-800"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm text-zinc-300">Yeni Parola (Tekrar)</label>
              <Input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Yeni parolani tekrar gir"
                className="bg-white dark:bg-zinc-950 border-zinc-300 dark:border-zinc-800"
              />
            </div>

            {error ? <p className="text-sm text-red-400">{error}</p> : null}
            {success ? <p className="text-sm text-emerald-400">{success}</p> : null}

            <Button type="submit" disabled={saving} className="w-full">
              {saving ? (
                <span className="inline-flex items-center gap-2">
                  <Loader2 className="size-4 animate-spin" />
                  Kaydediliyor
                </span>
              ) : (
                "Parolayi Guncelle"
              )}
            </Button>
          </form>
        ) : null}
          </div>
        </div>
    </div>
  );
}

export default SettingsPage;
