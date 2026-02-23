CREATE TABLE forest_zone (
                             zone_id SERIAL PRIMARY KEY,
                             zone_name VARCHAR(100),
                             total_area_ha DECIMAL(10,2),
                             forest_area_ha DECIMAL(10,2),
                             center_lon DECIMAL(10,6),
                             center_lat DECIMAL(10,6),
                             geom GEOMETRY(POINT, 4326)
);

-- 2. 林分表（核心）
CREATE TABLE forest_stand (
                              stand_id SERIAL PRIMARY KEY,
                              zone_id INTEGER REFERENCES forest_zone(zone_id),
                              xiao_ban_code VARCHAR(20), -- 如：01-05
                              area_ha DECIMAL(8,2),
                              dominant_species VARCHAR(50),
                              species_composition JSONB, -- [{"species":"马尾松","ratio":0.7},...]
                              volume_per_ha DECIMAL(8,2),
                              total_volume DECIMAL(10,2),
                              center_lon DECIMAL(10,6),
                              center_lat DECIMAL(10,6),
                              geom GEOMETRY(POINT, 4326)
);

-- 3. 样地表
CREATE TABLE sample_plot (
                             plot_id SERIAL PRIMARY KEY,
                             stand_id INTEGER REFERENCES forest_stand(stand_id),
                             plot_no INTEGER,
                             plot_area_ha DECIMAL(6,4),
                             total_trees INTEGER,
                             avg_dbh DECIMAL(5,2),
                             avg_height DECIMAL(5,2),
                             volume_per_ha DECIMAL(8,2),
                             longitude_dd DECIMAL(10,6),
                             latitude_dd DECIMAL(10,6),
                             geom GEOMETRY(POINT, 4326)
);

-- 4. 单木表
CREATE TABLE tree_measurement (
                                  tree_id SERIAL PRIMARY KEY,
                                  plot_id INTEGER REFERENCES sample_plot(plot_id),
                                  tree_no INTEGER,
                                  species VARCHAR(50),
                                  dbh_avg DECIMAL(5,2),
                                  tree_height DECIMAL(5,2),
                                  diameter_half_height DECIMAL(5,2),
                                  q2 DECIMAL(8,4),
                                  f1 DECIMAL(8,4),
                                  basal_area DECIMAL(8,2),
                                  volume DECIMAL(10,4)
);