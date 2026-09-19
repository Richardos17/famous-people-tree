"use client"
import '@xyflow/react/dist/style.css';
import { ReactFlow, Background, Controls, addEdge, applyNodeChanges, applyEdgeChanges , MarkerType,type Node, type Edge, type OnConnect, type OnNodesChange, type OnEdgesChange, ConnectionMode, XYPosition, useEdgesState, useNodesState } from '@xyflow/react';
import {useState, useCallback, useRef, useMemo} from 'react';
import PersonNode from '@/components/PersonNode';
import RelationshipEdge from '@/components/RelationshipEdge';
import { Person } from '../types/Person';
import { PersonConnectedEdges } from '@/app/types/PersonConnectedEdges';
import type { HandlePosition } from '@/app/types/HandlePosition';
import { EdgeType } from '@/graph/edgeType';
import { FamilyGraph } from '@/types/FamilyGraph';
import {layoutFamilyTree} from '@/graph/layoutFamilyTree'
import { FamilyTreeLayout } from '@/types/FamilyTreeLayout';
import { fetchMockData } from '@/api/mockApi';
const nodeTypes = {
  person: PersonNode,
};
const edgeTypes = {
  parent: RelationshipEdge,
};
export default function FamilyTree(){
  
  

  const [graph, setGraph] = useState<FamilyGraph>(fetchMockData());

const initialLayout = useMemo<FamilyTreeLayout>(
  () => layoutFamilyTree(graph, "Q43577"),
  [graph]
);

const [nodes, setNodes] = useNodesState(initialLayout.nodes);
const [edges, setEdges] = useEdgesState(initialLayout.edges);
 
      
  const onConnect: OnConnect = useCallback((connection) => {
    setEdges((oldEdges) => addEdge<Edge>({...connection, type:"parent"}, oldEdges))
  }, [])
    const onNodesChange: OnNodesChange = useCallback((connection) => setNodes((oldEdges) => applyNodeChanges<Node>(connection, oldEdges)), [])
    const onEdgesChange: OnEdgesChange = useCallback((connection) => {setEdges((oldEdges) => applyEdgeChanges<Edge>(connection, oldEdges))}, [])

    return <div style={{width: 1000, height: 500}}>
      <ReactFlow nodes={nodes} onConnect={onConnect} connectionMode={ConnectionMode.Loose} edges={edges} onNodesChange={onNodesChange} onEdgesChange={onEdgesChange} nodeTypes={nodeTypes} edgeTypes={edgeTypes}>
        <Controls/>
        <Background/>
      </ReactFlow>
      <button
  onClick={() => {
    //TODO remove this and put into mock
   /* const rootNode:Node = addRootNode({wikidataId: "Q19",name: "Richard 1", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"})

      const parent:Node = addParent({wikidataId: "Q9",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, rootNode)
           addParent({wikidataId: "Q11",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, rootNode)
           addParent({wikidataId: "Q141",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, parent)
           addParent({wikidataId: "Q1441",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, rootNode)
           addChild({wikidataId: "Q14441",name: "Richard 3", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, rootNode)
          const married:Node = addMarriage({wikidataId: "Q741",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, rootNode)
          addMarriage({wikidataId: "Q74445",name: "Richard 2", birthDate: new Date(2014, 9, 9 ), deathDate: new Date(2014, 9, 19), birthCountry: "SK", imageLink: "https://storage.googleapis.com/media-newsinitiative/images/GO801_GNI_VerifyingPhotos_Card2_image3.original.jpg", wikipediaLink:"string"}, married)
*/

  }}
>
  Test
</button>
    </div>
}