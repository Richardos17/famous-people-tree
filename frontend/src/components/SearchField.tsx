"use client";
import { fetchSearchPersonByName } from "@/api/familyTreeApi";
import { Person } from "@/types/Person";
import { mapSearchResult } from "@/utils/FamilyGraphMapper";
import { useState } from "react";
type SearchFieldProps = {
    onShowTree: (personId: string) => void;
};
export default function SearchField({onShowTree}: SearchFieldProps) {
  const [searchedText, setSearchedText] = useState<string>("");
  const [submittedText, setSubmittedText] = useState<string>("");

  const [searchResults, setSearchResults] = useState<Person[]>([])
  const handleSubmit = () => {
    fetchSearchPersonByName(searchedText.trim()).then((data) => {          
          setSearchResults(mapSearchResult(data));
        });
        setSubmittedText(searchedText);
        setSearchedText("");
  }
  return (
    <>
      <div>
        <input
          type="text"
          value={searchedText}
          onChange={(e) => setSearchedText(e.target.value)}
        />
        <button onClick={handleSubmit}>Search</button>
      </div>
      <div>{submittedText !== "" ? `Searched for: ${submittedText}` : ""}</div>
      <div>
        {searchResults.map((person, index)=><div key={index}>{person.name} <button onClick={()=>onShowTree(person.wikidataId)}>Show tree</button></div>)}
      </div>
    </>
  );
}
