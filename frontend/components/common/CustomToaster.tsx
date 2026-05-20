"use client";

import React from "react";
import { Toaster } from "sonner";

export default function CustomToaster() {
  return (
    <Toaster
      position="top-right"
      richColors
      closeButton
    />
  );
}
