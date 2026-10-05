"use client";
import FamilyTree from "@/components/FamilyTree";
import SearchField from "@/components/SearchField";
import { useState } from "react";

export default function Home() {
  const [selectedPersonId, setSelectedPersonId] = useState("Q4787");
  const [selectedDepth, setSelectedDepth] = useState(1);

  return (
    <>
      <SearchField
        onShowTree={(personId, treeDepth) => {
          setSelectedPersonId(personId);
          setSelectedDepth(treeDepth);
        }}
      />
      <FamilyTree centerPersonId={selectedPersonId} depth={selectedDepth} />
    </>
  );
}
