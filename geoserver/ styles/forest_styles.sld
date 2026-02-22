<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.0.0"
    xmlns="http://www.opengis.net/sld"
    xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">

    <NamedLayer>
        <Name>forest_stand_style</Name>
        <UserStyle>
            <Name>forest_stand_style</Name>
            <Title>林分按树种渲染</Title>
            <FeatureTypeStyle>

                <!-- 马尾松 - 深绿 -->
                <Rule>
                    <Name>马尾松</Name>
                    <Title>马尾松</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>dominant_species</ogc:PropertyName>
                            <ogc:Literal>马尾松</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#2E7D32</CssParameter>
                            <CssParameter name="fill-opacity">0.7</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#1B5E20</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>

                <!-- 杉木 - 绿色 -->
                <Rule>
                    <Name>杉木</Name>
                    <Title>杉木</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>dominant_species</ogc:PropertyName>
                            <ogc:Literal>杉木</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#388E3C</CssParameter>
                            <CssParameter name="fill-opacity">0.7</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#2E7D32</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>

                <!-- 湿地松 - 浅绿 -->
                <Rule>
                    <Name>湿地松</Name>
                    <Title>湿地松</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>dominant_species</ogc:PropertyName>
                            <ogc:Literal>湿地松</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#43A047</CssParameter>
                            <CssParameter name="fill-opacity">0.7</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#2E7D32</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>

                <!-- 枫香 - 红色 -->
                <Rule>
                    <Name>枫香</Name>
                    <Title>枫香</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>dominant_species</ogc:PropertyName>
                            <ogc:Literal>枫香</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#D32F2F</CssParameter>
                            <CssParameter name="fill-opacity">0.7</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#B71C1C</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>

                <!-- 麻栎 - 棕色 -->
                <Rule>
                    <Name>麻栎</Name>
                    <Title>麻栎</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>dominant_species</ogc:PropertyName>
                            <ogc:Literal>麻栎</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#795548</CssParameter>
                            <CssParameter name="fill-opacity">0.7</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#5D4037</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>

                <!-- 香樟 - 青绿 -->
                <Rule>
                    <Name>香樟</Name>
                    <Title>香樟</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>dominant_species</ogc:PropertyName>
                            <ogc:Literal>香樟</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#00796B</CssParameter>
                            <CssParameter name="fill-opacity">0.7</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#004D40</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>

                <!-- 毛竹 - 黄绿 -->
                <Rule>
                    <Name>毛竹</Name>
                    <Title>毛竹</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>dominant_species</ogc:PropertyName>
                            <ogc:Literal>毛竹</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#689F38</CssParameter>
                            <CssParameter name="fill-opacity">0.7</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#33691E</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>

                <!-- 杂阔 - 灰色 -->
                <Rule>
                    <Name>杂阔</Name>
                    <Title>杂阔</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>dominant_species</ogc:PropertyName>
                            <ogc:Literal>杂阔</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#757575</CssParameter>
                            <CssParameter name="fill-opacity">0.7</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#424242</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>

                <!-- 默认样式 -->
                <Rule>
                    <Name>其他</Name>
                    <Title>其他树种</Title>
                    <ElseFilter/>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#9E9E9E</CssParameter>
                            <CssParameter name="fill-opacity">0.5</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#616161</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>

            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>