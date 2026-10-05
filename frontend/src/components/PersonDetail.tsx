import { Person } from "@/types/Person";
import Image from "next/image";

export default function PersonDetail({
  personData,
  onHideDetail,
}: {
  personData: Person;
  onHideDetail: () => void;
}) {
  return (
    <div className="w-50 absolute top-0 right-0 bg-white shadow-md overflow-hidden border z-10">
      <button
        onClick={() => onHideDetail()}
        className="bg-red-500 text-white font-bold absolute top-0 right-0 w-7 h-7 "
      >
        X
      </button>
      <div className="h-48 w-full">
        {personData.imageLink && personData.imageLink !== "" && (
          <Image
            unoptimized
            alt={personData.name}
            width={192}
            height={192}
            src={personData.imageLink}
            className="h-full w-full object-cover object-top"
          />
        )}
      </div>
      <div className="p-3">
        <h3 className="mb-2 truncate text-base text-center font-semibold text-gray-900">
          {personData.name}
        </h3>

        <div className="space-y-1 text-sm text-gray-600">
          {personData.birthDate && (
            <div>
              <span className="font-medium text-gray-700">Born:</span>{" "}
              {personData.birthDate.toLocaleDateString()}{personData.bornIn && ` (${personData.bornIn.name})` }
            </div>
          )}
          {personData.deathDate && (
            <div>
              <span className="font-medium text-gray-700">Died:</span>{" "}
              {personData.deathDate.toLocaleDateString()}
            </div>
          )}
          {personData.height && (
            <div>
              <span className="font-medium text-gray-700">Height:</span>{" "}
              {personData.height}
            </div>
          )}
        </div>
        {personData.wikipediaLink && (
          <a
            href={personData.wikipediaLink}
            target="_blank"
            rel="noopener noreferrer"
            className="mt-3 block text-sm font-medium text-blue-600 hover:text-blue-800 hover:underline"
          >
            Wikipedia
          </a>
        )}
      </div>
    </div>
  );
}
