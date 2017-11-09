CREATE TABLE ARTICLE (
  pmc_id  TEXT PRIMARY KEY NOT NULL,
  title   TEXT
);

CREATE TABLE TRAIT_TABLE (
  tab_id  TEXT PRIMARY KEY NOT NULL,
  n_cols  INTEGER NOT NULL,
  n_rows  INTEGER NOT NULL,
  pmc_id  TEXT NOT NULL,
  FOREIGN KEY(pmc_id) REFERENCES ARTICLE(pmc_id)
);

CREATE TABLE ABBREVIATION (
  abbrev    TEXT NOT NULL,
  expansion TEXT NOT NULL,
  pmc_id    TEXT NOT NULL,
  FOREIGN KEY(pmc_id) REFERENCES ARTICLE(pmc_id)
);

CREATE TABLE COLUMN_ENTRY (
  col_id  TEXT PRIMARY KEY NOT NULL,
  header  TEXT,
  type    TEXT,
  annot   TEXT,
  tab_id  TEXT,
  FOREIGN KEY(tab_id) REFERENCES TRAIT_TABLE(tab_id)
);

CREATE TABLE CELL_ENTRY (
  cel_id  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  row_id  INTEGER,
  value   TEXT,
  type    TEXT,
  col_id  TEXT,
  FOREIGN KEY(col_id) REFERENCES COLUMN_ENTRY(col_id)
);

CREATE TABLE TRAIT (
  trait_id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  trait_name        TEXT,
  trait_value_pair  TEXT,
  trait_prop_pair   TEXT,
  other_trait_pair  TEXT,
  pmc_id            TEXT,
  tab_id            TEXT,
  row_id            INTEGER,
  FOREIGN KEY(pmc_id) REFERENCES ARTICLE(pmc_id),
  FOREIGN KEY(tab_id) REFERENCES TRAIT_TABLE(tab_id)
);

CREATE TABLE QTL (
  qtl_id            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  trait_in_article  TEXT,
  trait_in_onto     TEXT,
  trait_uri         TEXT, --> trait_id?
  chromosome        TEXT,
  marker            TEXT,
  marker_uri        TEXT, --> marker_id?
  gene              TEXT,
  gene_uri          TEXT, --> gene_id?
  --snpAssociated     TEXT, --> marker
  --snpUri            TEXT, --> marker_uri
  pmc_id            TEXT,
  tab_id            TEXT,
  row_id            INTEGER,
  FOREIGN KEY(pmc_id) REFERENCES ARTICLE(pmc_id),
  FOREIGN KEY(tab_id) REFERENCES TRAIT_TABLE(tab_id)
);
