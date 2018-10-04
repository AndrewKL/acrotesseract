CREATE TABLE Poses (
  pose_id int(11) NOT NULL AUTO_INCREMENT,
  created_ts timestamp NOT NULL,
  name varchar(256) NOT NULL,
  createdBy varchar(256) NOT NULL,
  image_url varchar(256) NOT NULL,
  PRIMARY KEY (pose_id)
);

CREATE TABLE Transitions (
  transition_id int(11) NOT NULL AUTO_INCREMENT,
  created_ts timestamp NOT NULL,
  name varchar(256) NOT NULL,
  createdBy varchar(256) NOT NULL,
  pose_from int(11) NOT NULL,
  pose_to int(11) NOT NULL,
  PRIMARY KEY (transition_id),
  FOREIGN KEY (pose_from) REFERENCES Poses (pose_id),
  FOREIGN KEY (pose_to) REFERENCES Poses (pose_id)
)