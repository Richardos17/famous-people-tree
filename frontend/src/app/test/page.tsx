"use client";
import { useEffect } from "react";
import { testTree } from "@/graph/reinholdAlgo"; // export it from wherever it lives

export default function TestPage() {
  useEffect(() => {
    testTree();
  }, []);

  return <div>Check the console</div>;
}