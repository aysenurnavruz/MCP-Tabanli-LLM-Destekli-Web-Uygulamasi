import { useEffect, useState } from "react";

type Theme = "light" | "dark" | "system";

const THEME_STORAGE_KEY = "app-theme";

export function useTheme() {
  const [theme, setTheme] = useState<Theme>(() => {
    const saved = localStorage.getItem(THEME_STORAGE_KEY) as Theme | null;
    return saved || "system";
  });

  useEffect(() => {
    localStorage.setItem(THEME_STORAGE_KEY, theme);

    const root = document.documentElement;
    let finalTheme: "light" | "dark" = theme === "system" 
      ? window.matchMedia("(prefers-color-scheme: dark)").matches 
        ? "dark" 
        : "light"
      : theme;

    if (finalTheme === "dark") {
      root.classList.add("dark");
    } else {
      root.classList.remove("dark");
    }
  }, [theme]);

  return { theme, setTheme };
}
