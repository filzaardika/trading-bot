/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "ui-monospace", "SFMono-Regular", "monospace"],
      },
      colors: {
        bg: { base: "#07090D", surface: "#0E131B", surfaceAlt: "#141B26", elev: "#1B2434" },
        border: { subtle: "#222C3B", default: "#2A3646" },
        text: { primary: "#F4F6FA", secondary: "#A7B0BE", muted: "#6B7584", disabled: "#424C5A" },
        accent: {
          indigo: "#6366F1",
          indigoDim: "#4F46E5",
          teal: "#2DD4BF",
          red: "#EF4444",
          amber: "#F59E0B",
        },
      },
      boxShadow: {
        card: "0 1px 0 0 rgba(255,255,255,0.04) inset, 0 1px 2px 0 rgba(0,0,0,0.4)",
        glow: "0 0 0 1px rgba(99,102,241,0.35), 0 8px 32px -8px rgba(99,102,241,0.45)",
      },
      animation: {
        "pulse-dot": "pulseDot 2s ease-out infinite",
        "ring-pulse": "ringPulse 2s ease-out infinite",
      },
      keyframes: {
        pulseDot: {
          "0%, 100%": { opacity: "1" },
          "50%": { opacity: "0.4" },
        },
        ringPulse: {
          "0%": { transform: "scale(1)", opacity: "0.6" },
          "100%": { transform: "scale(2.2)", opacity: "0" },
        },
      },
    },
  },
  plugins: [],
};
