"use client";
import { fetchSearchPersonByName } from "@/api/familyTreeApi";
import { Person } from "@/types/Person";
import { mapSearchResult } from "@/utils/FamilyGraphMapper";
import { tree } from "d3";
import { useState } from "react";
type SearchFieldProps = {
  onShowTree: (personId: string, treeDepth: number) => void;
};
export default function SearchField({ onShowTree }: SearchFieldProps) {
  const [searchedText, setSearchedText] = useState<string>("");
  const [submittedText, setSubmittedText] = useState<string>("");
  const [treeDepth, setTreeDepth] = useState<number>(1);


  const [searchResults, setSearchResults] = useState<Person[]>([]);
  const handleSubmit = () => {
    fetchSearchPersonByName(searchedText.trim()).then((data) => {
      setSearchResults(mapSearchResult(data));
    });
    setSubmittedText(searchedText);
    setSearchedText("");
  };
  return (
    <div className="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-4 shadow-lg">
      <div className="flex gap-2">
        <input
          type="text"
          value={searchedText}
          onChange={(e) => setSearchedText(e.target.value)}
          className="min-w-0 flex-1 rounded-xl border border-gray-300 bg-gray-50 px-4 py-2.5 text-sm text-gray-900 outline-none transition placeholder:text-gray-400 focus:border-blue-500 focus:bg-white focus:ring-2 focus:ring-blue-100"
        />
        <button
          className="rounded-xl bg-blue-600 px-5 py-2.5 text-sm font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-300"
          onClick={handleSubmit}
        >
          Search
        </button>
      </div>
      <div className="font-semibold text-gray-900">
        {submittedText !== "" ? `Searched for: ${submittedText}` : ""}
      </div>
      <div className="font-semibold text-gray-900">
        {"Tree depth: "}
        <input
          type="number"
          value={treeDepth}
          onChange={(e) => setTreeDepth(Number(e.target.value))}
          className="min-w-0 w-20 rounded-xl border border-gray-300 bg-gray-50 px-4 py-2.5 text-sm text-gray-900 outline-none transition placeholder:text-gray-400 focus:border-blue-500 focus:bg-white focus:ring-2 focus:ring-blue-100"
        />
      </div>
      <div className="mt-3 max-h-80 overflow-y-auto pr-1">
        {searchResults.map((person, index) => (
          <div
            key={index}
            className="flex items-center justify-between gap-3 rounded-xl border border-gray-100 bg-gray-50 p-3 transition hover:border-blue-100 hover:bg-blue-50"
          >
            <p className="truncate text-sm font-semibold text-gray-900">
                    {person.name}
                  </p>
            <button
              className="shrink-0 rounded-lg bg-gray-900 px-3 py-2 text-xs font-medium text-white transition hover:bg-blue-600"
              onClick={() => onShowTree(person.wikidataId, treeDepth)}
            >
              Show tree
            </button>
          </div>
        ))}
      </div>
      <div />
    </div>
  );
}
