import { Handle, Position, type Node, type NodeProps } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import Image from 'next/image';
import { Person } from './types/Person';
import { type HandlePosition } from './types/HandlePosition';

type PersonNode = Node<{ person:Person, handlePosition:HandlePosition}, 'person'>;
export default function PersonNode({id, data }: NodeProps<PersonNode>){

  return (
  <div className="w-48 h-64 overflow-hidden rounded-lg border border-gray-200 bg-white shadow-md">
    {(data.handlePosition.top) && <Handle type="source" position={Position.Top} id={"t"+id} />}
    {(data.handlePosition.left) && <Handle type="source" position={Position.Left} id={"l"+id} />}
    {(data.handlePosition.right) && <Handle type="source" position={Position.Right} id={"r"+id} />}
    {/* Image */}
    <div className="h-28 w-full">
      <Image
        alt={data.person.name}
        width={192}
        height={112}
        src={data.person.imageLink}
        className="h-full w-full object-cover"
      />
    </div>

    {/* Data */}
    <div className="p-3">
      <h3 className="mb-2 truncate text-base font-semibold text-gray-900">
        {data.person.name}
      </h3>

      <div className="space-y-1 text-sm text-gray-600">
        <div>
          <span className="font-medium text-gray-700">Born:</span>{" "}
          {data.person.birthDate.toLocaleDateString()}
        </div>

        <div>
          <span className="font-medium text-gray-700">Died:</span>{" "}
          {data.person.deathDate.toLocaleDateString()}
        </div>
      </div>

      <a
        href={data.person.wikipediaLink}
        target="_blank"
        rel="noopener noreferrer"
        className="mt-3 block text-sm font-medium text-blue-600 hover:text-blue-800 hover:underline"
      >
        Wikipedia
      </a>
    </div>
    {(data.handlePosition.bottom) && <Handle type="source" position={Position.Bottom} id={"b"+id}/>}
  </div>
);
} 
