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
const nodeTypes = {
  person: PersonNode,
};
const edgeTypes = {
  parent: RelationshipEdge,
};
type FamilyTreeProps = {
    centerPersonId?: string;
};
export default function FamilyTree({centerPersonId}:FamilyTreeProps) {
  const [graph, setGraph] = useState<FamilyGraph>({
    persons: new Map(),
    relationships: [],
  });

  useEffect(() => {
    if(!centerPersonId || centerPersonId === "")
      return
    fetchBasicTree(centerPersonId).then((data) => {
      const familyGraph = mapResponseToFamilyGraph(data);
      setGraph(familyGraph);
    });
  }, [centerPersonId]);

  const initialLayout = useMemo<FamilyTreeLayout>(
    () =>
      graph.persons.size > 0
        ? layoutFamilyTree(graph, centerPersonId)
        : { nodes: [], edges: [] },
    [graph],
  );

  const [nodes, setNodes] = useNodesState(initialLayout.nodes);
  const [edges, setEdges] = useEdgesState(initialLayout.edges);
  useEffect(() => {
    setNodes(initialLayout.nodes);
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
    <div style={{ width: 1000, height: 500 }}>
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
