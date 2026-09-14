import { Handle, Position, type Node, type NodeProps } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { useState} from 'react';
import Image from 'next/image';
export enum VerticalHandlePosition {
  Top,
  Bottom,
  Both,
  None
}
export enum HorizontalHandlePosition {
  Left,
  Right,
  Both,
  None
}
export type HandlePosition = {
  verticalHandlePosition: VerticalHandlePosition;
  horizontalHandlePosition: HorizontalHandlePosition;
};
type PersonNode = Node<{ name: string,  birthDate: Date, deathDate: Date, imageLink: string, wikipediaLink:string, handlePosition:HandlePosition}, 'person'>;
export default function PersonNode({id, data }: NodeProps<PersonNode>){

  return (
  <div className="w-48 h-64 overflow-hidden rounded-lg border border-gray-200 bg-white shadow-md">
    {(data.handlePosition.verticalHandlePosition == VerticalHandlePosition.Top
      || data.handlePosition.verticalHandlePosition == VerticalHandlePosition.Both
    ) && <Handle type="source" position={Position.Top} id={"t"+id} />}
    {/* Image */}
    <div className="h-28 w-full">
      <Image
        alt={data.name}
        width={192}
        height={112}
        src={data.imageLink}
        className="h-full w-full object-cover"
      />
    </div>

    {/* Data */}
    <div className="p-3">
      <h3 className="mb-2 truncate text-base font-semibold text-gray-900">
        {data.name}
      </h3>

      <div className="space-y-1 text-sm text-gray-600">
        <div>
          <span className="font-medium text-gray-700">Born:</span>{" "}
          {data.birthDate.toLocaleDateString()}
        </div>

        <div>
          <span className="font-medium text-gray-700">Died:</span>{" "}
          {data.deathDate.toLocaleDateString()}
        </div>
      </div>

      <a
        href={data.wikipediaLink}
        target="_blank"
        rel="noopener noreferrer"
        className="mt-3 block text-sm font-medium text-blue-600 hover:text-blue-800 hover:underline"
      >
        Wikipedia
      </a>
    </div>
    {(data.handlePosition.verticalHandlePosition == VerticalHandlePosition.Bottom
      || data.handlePosition.verticalHandlePosition == VerticalHandlePosition.Both
    ) && <Handle type="source" position={Position.Bottom} id={"b"+id}/>}
  </div>
);
} 
