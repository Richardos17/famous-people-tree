import { Handle, Position, type Node, type NodeProps } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import Image from "next/image";
import { Person } from "../types/Person";
import { type HandlePosition } from "../app/types/HandlePosition";

type PersonNode = Node<
  { person: Person; handlePosition: HandlePosition; onShowDetail: (personData:Person) => void},
  "person"
>;
export default function PersonNode({ id, data }: NodeProps<PersonNode>) {
  return (
    <button onClick={() => data.onShowDetail(data.person)} className="w-38 overflow-hidden rounded-lg border border-gray-200 bg-white shadow-md flex flex-col">
      {data.handlePosition.top && (
        <Handle type="source" position={Position.Top} id={"t" + id} />
      )}
      {data.handlePosition.left && (
        <Handle type="source" position={Position.Left} id={"l" + id} />
      )}
      {data.handlePosition.right && (
        <Handle type="source" position={Position.Right} id={"r" + id} />
      )}
      <div className="h-38 w-full">
        {data.person.imageLink && data.person.imageLink !== "" && (
          <Image
            unoptimized
            alt={data.person.name}
            width={192}
            height={192}
            src={data.person.imageLink}
            className="h-full w-full object-cover object-top"
          />
        )}
      </div>

      <div className="p-3">
        <h3 className="truncate text-base font-semibold text-gray-900">
          {data.person.name}
        </h3>
        <span className="space-y-1 text-sm font-medium text-gray-700">
                  {data.person.wikidataId + "\n"}
          {(data.person.birthDate
            ? data.person.birthDate.getFullYear().toString()
            : "Unknown") +
            " - " +
            (data.person.deathDate
              ? data.person.deathDate.getFullYear().toString()
              : "Unknown")}
        </span>
      </div>
      {data.handlePosition.bottom && (
        <Handle type="source" position={Position.Bottom} id={"b" + id} />
      )}
    </button>
  );
}
