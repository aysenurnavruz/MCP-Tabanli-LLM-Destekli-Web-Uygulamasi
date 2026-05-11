import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useAuth } from "../hooks/useAuth";
import { loginSchema, type LoginFormValues } from "../schemas/authSchema";

function LoginPage() {
  const navigate = useNavigate();
  const { login, loading, error } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
    mode: "onSubmit",
  });

  const onSubmit = async (values: LoginFormValues) => {
    const ok = await login(values);
    if (ok) navigate("/chat");
  };

  const isBusy = loading || isSubmitting;

  return (
    <div className="min-h-screen grid place-items-center bg-white dark:bg-neutral-950 text-zinc-900 dark:text-white px-4">
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="w-full max-w-md rounded-2xl border border-zinc-200 dark:border-white/10 bg-white dark:bg-white/5 p-6"
        noValidate
      >
        <h1 className="text-2xl font-semibold">Giriş Yap</h1>

        <div className="mt-6 space-y-3">
          <div>
            <input
              className="w-full rounded-lg border border-zinc-300 bg-white px-3 py-2 text-zinc-900 placeholder-zinc-500 dark:border-white/15 dark:bg-black/30 dark:text-white dark:placeholder-zinc-400"
              placeholder="E-posta"
              type="email"
              autoComplete="email"
              {...register("email")}
            />
            {errors.email ? (
              <p className="mt-1 text-sm text-red-400">{errors.email.message}</p>
            ) : null}
          </div>

          <div>
            <input
              type="password"
              className="w-full rounded-lg border border-zinc-300 bg-white px-3 py-2 text-zinc-900 placeholder-zinc-500 dark:border-white/15 dark:bg-black/30 dark:text-white dark:placeholder-zinc-400"
              placeholder="Şifre"
              autoComplete="current-password"
              {...register("password")}
            />
            {errors.password ? (
              <p className="mt-1 text-sm text-red-400">{errors.password.message}</p>
            ) : null}
          </div>

          <button
            disabled={isBusy}
            className="w-full rounded-lg bg-zinc-900 text-white py-2 font-medium disabled:opacity-60 dark:bg-white dark:text-black"
          >
            {isBusy ? "Giriş yapılıyor..." : "Giriş"}
          </button>
        </div>

        {error ? <p className="mt-3 text-sm text-red-400">{error}</p> : null}

        <p className="mt-4 text-sm text-zinc-600 dark:text-white/70">
          Hesabın yok mu?{" "}
          <Link to="/register" className="text-zinc-900 underline dark:text-white">
            Kayıt ol
          </Link>
        </p>
      </form>
    </div>
  );
}

export default LoginPage;