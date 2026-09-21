'use client'
import FamilyTree from "@/components/FamilyTree";
import SearchField from "@/components/SearchField";
import { useState } from "react";

export default function Home(){  
    const [selectedPersonId, setSelectedPersonId] = useState("");
    return<>
    <SearchField onShowTree={setSelectedPersonId}/>
    <FamilyTree centerPersonId={selectedPersonId}/>
    </> 
}