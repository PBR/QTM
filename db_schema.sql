CREATE TABLE Article (
  pmcId   TEXT PRIMARY KEY NOT NULL,
  tittle  TEXT
);

CREATE TABLE TraitTable (
  tableId   TEXT PRIMARY KEY NOT NULL,
  numOfCol  INTEGER NOT NULL,
  numOfRows INTEGER NOT NULL,
  pmcId     TEXT NOT NULL,
  FOREIGN KEY(pmcId) REFERENCES article(pmcId)
);

CREATE TABLE Abbreviation (
  abbreviation  TEXT NOT NULL,
  expansion     TEXT NOT NULL,
  pmcId         TEXT NOT NULL,
  FOREIGN KEY(pmcId) REFERENCES Article(pmcId)
);

CREATE TABLE ColumnEntry (
  colId         TEXT PRIMARY KEY NOT NULL,
  colHeader     TEXT,
  colType       TEXT,
  colAnnotation TEXT,
  tableId       TEXT,
  FOREIGN KEY(tableId) REFERENCES TraitTable(tableId)
);

CREATE TABLE CellEntry (
  cellId    INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  rowNumber INT,
  cellValue TEXT,
  cellType  TEXT,
  colId     TEXT,
  FOREIGN KEY(colId) REFERENCES columnEntry(colId)
);

CREATE TABLE Trait (
  traitId           INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  traitName         Text,
  traitValuePair    TEXT,
  traitPropertyPair TEXT,
  otherTraitPair    TEXT,
  pmcId             TEXT,
  tableId           TEXT,
  rowNumber         INTEGER,
  FOREIGN KEY(pmcId) REFERENCES Article(pmcId),
  FOREIGN KEY(tableId) REFERENCES TraitTable(tableId)
);

CREATE TABLE Qtl (
  qtlId               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  traitNameInArticle  Text,
  traitNameInOntology Text,
  traitUri            TEXT,
  chromosomeNumber    TEXT,
  markerAssociated    TEXT,
  markerUri           TEXT,
  geneAssociated      TEXT,
  geneUri             TEXT,
  snpAssociated       TEXT,
  snpUri              TEXT,
  pmcId               TEXT,
  tableId             TEXT,
  rowNumber           TEXT,
  FOREIGN KEY(pmcId) REFERENCES Article(pmcId),
  FOREIGN KEY(tableId) REFERENCES TraitTable(tableId)
);
