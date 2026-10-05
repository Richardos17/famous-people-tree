"use client";
import "@xyflow/react/dist/style.css";
import {
  ReactFlow,
  Background,
  Controls,
  addEdge,
  applyNodeChanges,
  applyEdgeChanges,
  MarkerType,
  type Node,
  type Edge,
  type OnConnect,
  type OnNodesChange,
  type OnEdgesChange,
  ConnectionMode,
  XYPosition,
  useEdgesState,
  useNodesState,
} from "@xyflow/react";
import { useState, useCallback, useRef, useMemo, useEffect } from "react";
import PersonNode from "@/components/PersonNode";
import RelationshipEdge from "@/components/RelationshipEdge";
import { Person } from "../types/Person";
import { PersonConnectedEdges } from "@/app/types/PersonConnectedEdges";
import type { HandlePosition } from "@/app/types/HandlePosition";
import { EdgeType } from "@/graph/edgeType";
import { FamilyGraph } from "@/types/FamilyGraph";
import { layoutFamilyTree } from "@/graph/layoutFamilyTree";
import { FamilyTreeLayout } from "@/types/FamilyTreeLayout";
import { fetchMockData } from "@/api/mockApi";
import { fetchBasicTree } from "@/api/familyTreeApi";
import { mapResponseToFamilyGraph } from "@/utils/FamilyGraphMapper";
import PersonDetail from "./PersonDetail";
const nodeTypes = {
  person: PersonNode,
};
const edgeTypes = {
  parent: RelationshipEdge,
};
type FamilyTreeProps = {
    centerPersonId?: string,
    depth?: number
};
export default function FamilyTree({centerPersonId, depth}:FamilyTreeProps) {
  const [graph, setGraph] = useState<FamilyGraph>({
    persons: new Map(),
    relationships: [],
  });
  const [selectedPerson, setSelectedPerson] = useState<Person | null>();
  
  useEffect(() => {
    if(!centerPersonId || centerPersonId === "")
      return
    if(!depth || depth <= 0)
      return
    fetchBasicTree(centerPersonId, depth).then((data) => {
      console.log(data)
      const familyGraph = mapResponseToFamilyGraph(data);
      setGraph(familyGraph);
    });
    setGraph(fetchMockData)
  }, [centerPersonId, depth]);

  const initialLayout = useMemo<FamilyTreeLayout>(
    () =>
      graph.persons.size > 0
        ? layoutFamilyTree(graph, centerPersonId)
        : { nodes: [], edges: [] },
    [graph],
  );

  const [nodes, setNodes] = useNodesState(initialLayout.nodes);
  const [edges, setEdges] = useEdgesState(initialLayout.edges);

   const handleShowDetail = (person: Person) => {    
    setSelectedPerson(person);
  };
  const handleHideDetail = () => {    
    setSelectedPerson(null);
  };
  useEffect(() => {
    setNodes(initialLayout.nodes.map(node=>({...node, data: {...node.data, onShowDetail: handleShowDetail}})));
    setEdges(initialLayout.edges);
  }, [initialLayout, setNodes, setEdges]);
  const onConnect: OnConnect = useCallback((connection) => {
    setEdges((oldEdges) =>
      addEdge<Edge>({ ...connection, type: "parent" }, oldEdges),
    );
  }, []);
  const onNodesChange: OnNodesChange = useCallback(
    (connection) =>
      setNodes((oldEdges) => applyNodeChanges<Node>(connection, oldEdges)),
    [],
  );
  const onEdgesChange: OnEdgesChange = useCallback((connection) => {
    setEdges((oldEdges) => applyEdgeChanges<Edge>(connection, oldEdges));
  }, []);
 
  return (
    <div style={{ width: '1000px', height: '900px' }}>
      {selectedPerson && <PersonDetail personData={selectedPerson} onHideDetail={handleHideDetail}/>}      
      <ReactFlow
        nodes={nodes}
        onConnect={onConnect}
        connectionMode={ConnectionMode.Loose}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
      >
        <Controls />
        <Background />
      </ReactFlow>
    </div>
  );
}
