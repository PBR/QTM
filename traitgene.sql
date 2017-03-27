select DISTINCT TraitOriginalName, TraitAnnotation, TraitAnnotationID, Property, ProAnnotation, ProAnnotationID, ActualProValue, pmcId, tableid
from traitProperties where (Property like '%gene%' or Property like '%marker%' or ActualProValue like '%sol%' or ActualProValue like '%snp%' ) and (ActualProValue not like '%(%)%' and  ActualProValue not like '%/%') 
