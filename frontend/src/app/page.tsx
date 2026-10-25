"use client";
import { fetchBasicTree } from "@/api/familyTreeApi";
import { fetchMockData } from "@/api/mockApi";
import FamilyTree from "@/components/FamilyTree";
import SearchField from "@/components/SearchField";
import { publicConfig } from "@/config/env";
import { ApiTreeResponse } from "@/types/apiTypes/ApiTreeResponse";
import { useEffect, useState } from "react";

export default function Home() {
  const [mock] = useState(() =>
    publicConfig.mockEnabled ? fetchMockData() : null
  );
  const [selectedPersonId, setSelectedPersonId] = useState(
    mock?.centerPersonId ?? ""
  );
  const [selectedDepth, setSelectedDepth] = useState(mock?.depth ?? 1);
  const [apiResponse, setApiResponse] = useState<ApiTreeResponse | null>(
    mock?.apiResponse ?? null
  );
  useEffect(() => {

      if(publicConfig.mockEnabled){
        return;
      }
      if(!selectedPersonId || selectedPersonId === "")
        return
      if(!selectedDepth || selectedDepth <= 0)
        return
      
      fetchBasicTree(selectedPersonId, selectedDepth).then((data) => {
        console.log(data)
        setApiResponse(data);
      });
      
    }, [selectedPersonId, selectedDepth]);
  return (
    <>
      <SearchField
        onShowTree={(personId, treeDepth) => {
          setSelectedPersonId(personId);
          setSelectedDepth(treeDepth);
        }}
      />
      <FamilyTree centerPersonId={selectedPersonId} apiResponse={apiResponse} />
    </>
  );
}
