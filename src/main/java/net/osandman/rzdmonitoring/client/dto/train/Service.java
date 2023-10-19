package net.osandman.rzdmonitoring.client.dto.train;

import lombok.Data;

@Data
public class Service {
    public int id;
    public String name;
    public String description;
    public boolean hasImage;
}
