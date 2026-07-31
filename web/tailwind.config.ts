import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#1264C5",
        "primary-light": "#8EC1FF",
        "on-primary": "#F8FBFF",
        "primary-container": "#D6E8FF",
        "on-primary-container": "#081B33",
        secondary: "#E6652F",
        "secondary-light": "#FF9A6B",
        "on-secondary": "#081B33",
        "secondary-container": "#FFE0D2",
        "on-secondary-container": "#55200F",
        tertiary: "#009E83",
        "tertiary-light": "#59D8C0",
        "on-tertiary": "#F8FBFF",
        "tertiary-container": "#C8F2E9",
        "on-tertiary-container": "#00382D",
        background: "#F3F7FC",
        "on-background": "#081B33",
        surface: "#F8FBFF",
        "on-surface": "#081B33",
        "surface-variant": "#E7EEF6",
        "on-surface-variant": "#3D536B",
        outline: "#7089A3",
        "outline-variant": "#B5C9DF",
      },
    },
  },
  plugins: [],
};

export default config;
