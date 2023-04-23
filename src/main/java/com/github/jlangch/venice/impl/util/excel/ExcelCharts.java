package com.github.jlangch.venice.impl.util.excel;

import java.util.List;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisCrossBetween;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFArea3DChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFAreaChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFBar3DChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLine3DChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.jlangch.venice.ExcelException;
import com.github.jlangch.venice.util.excel.CellAddr;
import com.github.jlangch.venice.util.excel.CellRangeAddr;
import com.github.jlangch.venice.util.excel.chart.AreaDataSeries;
import com.github.jlangch.venice.util.excel.chart.BarDataSeries;
import com.github.jlangch.venice.util.excel.chart.BarGrouping;
import com.github.jlangch.venice.util.excel.chart.ImageType;
import com.github.jlangch.venice.util.excel.chart.LineDataSeries;
import com.github.jlangch.venice.util.excel.chart.MarkerStyle;
import com.github.jlangch.venice.util.excel.chart.PieDataSeries;
import com.github.jlangch.venice.util.excel.chart.Position;


public class ExcelCharts {

	public ExcelCharts(final Sheet sheet) {
		this.sheet = sheet;
	}

    public void addImage(
            final CellAddr anchor,
            final byte[] data,
            final ImageType type,
            final Double scaleX,
            final Double scaleY
    ) {
        switch(type) {
            case PNG:
                setImage(anchor, data, Workbook.PICTURE_TYPE_PNG, scaleX, scaleY);
                break;
            case JPEG:
                setImage(anchor, data, Workbook.PICTURE_TYPE_JPEG, scaleX, scaleY);
                break;
            default:
                throw new ExcelException(String.format(
                        "Excel cell %s in sheet '%s': Invalid image type. Use PNG or JPEG",
                        anchor.mapToOneBased(),
                        sheet.getSheetName()));
        }
    }

    public void addLineChart(
            final String title,
            final CellRangeAddr areaCellRangeAddr,
            final Position legendPosition,
            final String categoryAxisTitle,
            final Position categoryAxisPosition,
            final String valueAxisTitle,
            final Position valueAxisPosition,
            final boolean threeDimensional,
            final boolean varyColors,
            final CellRangeAddr categoriesCellRangeAddr,
            final List<LineDataSeries> series
    ) {
        if (!(sheet.getWorkbook() instanceof XSSFWorkbook)) {
            throw new ExcelException("Excel line charts only work with Excel of type XLSX!");
        }

        final XSSFDrawing drawing = (XSSFDrawing)sheet.createDrawingPatriarch();
        final XSSFClientAnchor anchor = drawingAnchor(drawing, areaCellRangeAddr);

        final XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);

        final XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(toLegendPosition(legendPosition));

        final XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(toAxisPosition(categoryAxisPosition));
        categoryAxis.setTitle(categoryAxisTitle);
        final XDDFValueAxis valueAxis = chart.createValueAxis(toAxisPosition(valueAxisPosition));
        valueAxis.setTitle(valueAxisTitle);

        final XDDFDataSource<String> categories = stringDataSource(categoriesCellRangeAddr);

        final XDDFChartData data;

        if (threeDimensional) {
            final XDDFLine3DChartData data_ = (XDDFLine3DChartData)chart.createData(
                                                ChartTypes.LINE3D,
                                                categoryAxis,
                                                valueAxis);
            data = data_;
        }
        else {
            final XDDFLineChartData data_ = (XDDFLineChartData)chart.createData(
                                                ChartTypes.LINE,
                                                categoryAxis,
                                                valueAxis);
            data = data_;
        }
        data.setVaryColors(varyColors);

        for(LineDataSeries s : series) {
            final XDDFNumericalDataSource<Double> values = numericalDataSource(s.getCellRangeAddr());

            if (threeDimensional) {
                final XDDFLine3DChartData.Series series_ = (XDDFLine3DChartData.Series)data.addSeries(categories, values);
                series_.setTitle(s.getTitle(), null);
                series_.setSmooth(s.isSmooth());
                series_.setMarkerStyle(toMarkerStyle(s.getMarkerStyle()));
            }
            else {
                final XDDFLineChartData.Series series_ = (XDDFLineChartData.Series)data.addSeries(categories, values);
                series_.setTitle(s.getTitle(), null);
                series_.setSmooth(s.isSmooth());
                series_.setMarkerStyle(toMarkerStyle(s.getMarkerStyle()));
            }
        }

        chart.plot(data);
    }

    public void addBarChart(
            final String title,
            final CellRangeAddr areaCellRangeAddr,
            final Position legendPosition,
            final String categoryAxisTitle,
            final Position categoryAxisPosition,
            final String valueAxisTitle,
            final Position valueAxisPosition,
            final boolean threeDimensional,
            final boolean directionBar,
            final BarGrouping grouping,
            final boolean varyColors,
            final CellRangeAddr categoriesCellRangeAddr,
            final List<BarDataSeries> series
    ) {
        if (!(sheet.getWorkbook() instanceof XSSFWorkbook)) {
            throw new ExcelException("Excel bar charts only work with Excel of type XLSX!");
        }

        final XSSFDrawing drawing = (XSSFDrawing)sheet.createDrawingPatriarch();
        final XSSFClientAnchor anchor = drawingAnchor(drawing, areaCellRangeAddr);

        final XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);

        final XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(toLegendPosition(legendPosition));

        final XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(toAxisPosition(categoryAxisPosition));
        categoryAxis.setTitle(categoryAxisTitle);
        final XDDFValueAxis valueAxis = chart.createValueAxis(toAxisPosition(valueAxisPosition));
        valueAxis.setTitle(valueAxisTitle);
        valueAxis.setCrossBetween(AxisCrossBetween.BETWEEN);

        final XDDFDataSource<String> categories = stringDataSource(categoriesCellRangeAddr);

        final XDDFChartData data;

        if (threeDimensional) {
            final XDDFBar3DChartData data_ = (XDDFBar3DChartData)chart.createData(
                                                ChartTypes.BAR3D,
                                                categoryAxis,
                                                valueAxis);
            data_.setBarDirection(directionBar ? BarDirection.BAR : BarDirection.COL);
            data_.setBarGrouping(toBarGrouping(grouping));
            data = data_;
        }
        else {
            final XDDFBarChartData data_ = (XDDFBarChartData)chart.createData(
                                                ChartTypes.BAR,
                                                categoryAxis,
                                                valueAxis);
            data_.setBarDirection(directionBar ? BarDirection.BAR : BarDirection.COL);
            data_.setBarGrouping(toBarGrouping(grouping));
            data = data_;
        }
        data.setVaryColors(varyColors);

        for(BarDataSeries s : series) {
            final XDDFNumericalDataSource<Double> values = numericalDataSource(s.getCellRangeAddr());

            final XDDFChartData.Series series_ = data.addSeries(categories, values);
            series_.setTitle(s.getTitle(), null);
        }

        final boolean stacked = grouping == BarGrouping.STACKED || grouping == BarGrouping.PERCENT_STACKED;
        if (stacked) {
        	chart.getCTChart().getPlotArea().getBarChartArray(0).addNewOverlap().setVal((byte)100);
        }

        chart.plot(data);
    }

    public void addAreaChart(
            final String title,
            final CellRangeAddr areaCellRangeAddr,
            final Position legendPosition,
            final String categoryAxisTitle,
            final Position categoryAxisPosition,
            final String valueAxisTitle,
            final Position valueAxisPosition,
            final boolean threeDimensional,
            final CellRangeAddr categoriesCellRangeAddr,
            final List<AreaDataSeries> series
    ) {
        if (!(sheet.getWorkbook() instanceof XSSFWorkbook)) {
            throw new ExcelException("Excel area charts only work with Excel of type XLSX!");
        }

        final XSSFDrawing drawing = (XSSFDrawing)sheet.createDrawingPatriarch();
        final XSSFClientAnchor anchor = drawingAnchor(drawing, areaCellRangeAddr);

        final XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);

        final XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(toLegendPosition(legendPosition));

        final XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(toAxisPosition(categoryAxisPosition));
        categoryAxis.setTitle(categoryAxisTitle);
        final XDDFValueAxis valueAxis = chart.createValueAxis(toAxisPosition(valueAxisPosition));
        valueAxis.setTitle(valueAxisTitle);

        final XDDFDataSource<String> categories = stringDataSource(categoriesCellRangeAddr);

        final XDDFChartData data;

        if (threeDimensional) {
            final XDDFArea3DChartData data_ = (XDDFArea3DChartData)chart.createData(
                                                ChartTypes.AREA3D,
                                                categoryAxis,
                                                valueAxis);
            data = data_;
        }
        else {
            final XDDFAreaChartData data_ = (XDDFAreaChartData)chart.createData(
                                                ChartTypes.AREA,
                                                categoryAxis,
                                                valueAxis);
            data = data_;
        }

        for(AreaDataSeries s : series) {
            final XDDFNumericalDataSource<Double> values = numericalDataSource(s.getCellRangeAddr());

            final XDDFChartData.Series series_ = data.addSeries(categories, values);
            series_.setTitle(s.getTitle(), null);
        }

        chart.plot(data);
    }

    public void addPieChart(
            final String title,
            final CellRangeAddr areaCellRangeAddr,
            final Position legendPosition,
            final boolean threeDimensional,
            final boolean varyColors,
            final CellRangeAddr categoriesCellRangeAddr,
            final List<PieDataSeries> series
    ) {
        if (!(sheet.getWorkbook() instanceof XSSFWorkbook)) {
            throw new ExcelException("Excel pie charts only work with Excel of type XLSX!");
        }
        if (series.size() != 1) {
            throw new ExcelException("Excel pie chart must have exactly one series!");
        }

        final XSSFDrawing drawing = (XSSFDrawing)sheet.createDrawingPatriarch();
        final XSSFClientAnchor anchor = drawingAnchor(drawing, areaCellRangeAddr);

        final XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);

        final XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(toLegendPosition(legendPosition));

        final XDDFDataSource<String> categories = stringDataSource(categoriesCellRangeAddr);

        final XDDFChartData data = chart.createData(threeDimensional ? ChartTypes.PIE3D : ChartTypes.PIE, null, null);
        data.setVaryColors(varyColors);

        final XDDFNumericalDataSource<Double> values = numericalDataSource(series.get(0).getCellRangeAddr());

        final XDDFChartData.Series series_ = data.addSeries(categories, values);
        series_.setTitle(series.get(0).getTitle(), null);

        chart.plot(data);
    }

    private void setImage(
            final CellAddr anchorAddr,
            final byte[] data,
            final int imageType,
            final Double scaleX,
            final Double scaleY
    ) {
        final CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        final Drawing<?> drawing = sheet.createDrawingPatriarch();

        final int pictureIdx = sheet.getWorkbook().addPicture(data, imageType);

        final ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(anchorAddr.getCol());
        anchor.setRow1(anchorAddr.getRow());

        final Picture pict = drawing.createPicture(anchor, pictureIdx);
        if (scaleX == null || scaleY== null) {
            pict.resize();
        }
        else {
            pict.resize(scaleX, scaleY);
        }
    }

    private org.apache.poi.xddf.usermodel.chart.MarkerStyle toMarkerStyle(final MarkerStyle style) {
        if (style == null) {
            return org.apache.poi.xddf.usermodel.chart.MarkerStyle.NONE;
        }
        else {
            switch(style) {
                case CIRCLE:    return org.apache.poi.xddf.usermodel.chart.MarkerStyle.CIRCLE;
                case DASH:      return org.apache.poi.xddf.usermodel.chart.MarkerStyle.DASH;
                case DIAMOND:   return org.apache.poi.xddf.usermodel.chart.MarkerStyle.DIAMOND;
                case DOT:       return org.apache.poi.xddf.usermodel.chart.MarkerStyle.DOT;
                case NONE:      return org.apache.poi.xddf.usermodel.chart.MarkerStyle.NONE;
                case PICTURE:   return org.apache.poi.xddf.usermodel.chart.MarkerStyle.PICTURE;
                case PLUS:      return org.apache.poi.xddf.usermodel.chart.MarkerStyle.PLUS;
                case SQUARE:    return org.apache.poi.xddf.usermodel.chart.MarkerStyle.SQUARE;
                case STAR:      return org.apache.poi.xddf.usermodel.chart.MarkerStyle.STAR;
                case TRIANGLE:  return org.apache.poi.xddf.usermodel.chart.MarkerStyle.TRIANGLE;
                case X:          return org.apache.poi.xddf.usermodel.chart.MarkerStyle.X;
                default:        return org.apache.poi.xddf.usermodel.chart.MarkerStyle.NONE;
            }
        }
    }

    private org.apache.poi.xddf.usermodel.chart.BarGrouping toBarGrouping(final BarGrouping grouping) {
        if (grouping == null) {
            return org.apache.poi.xddf.usermodel.chart.BarGrouping.STANDARD;
        }
        else {
            switch(grouping) {
                case STANDARD:         return org.apache.poi.xddf.usermodel.chart.BarGrouping.STANDARD;
                case CLUSTERED:        return org.apache.poi.xddf.usermodel.chart.BarGrouping.CLUSTERED;
                case STACKED:          return org.apache.poi.xddf.usermodel.chart.BarGrouping.STACKED;
                case PERCENT_STACKED:  return org.apache.poi.xddf.usermodel.chart.BarGrouping.PERCENT_STACKED;
                default:               return org.apache.poi.xddf.usermodel.chart.BarGrouping.STANDARD;
            }
        }
    }

    private XSSFClientAnchor drawingAnchor(
            final XSSFDrawing drawing,
            final CellRangeAddr addr
    ) {
        return drawing.createAnchor(
                0, 0, 0, 0,
                addr.getFirstCol(),
                addr.getFirstRow(),
                addr.getLastCol(),
                addr.getLastRow());
    }

    private XDDFNumericalDataSource<Double> numericalDataSource(final CellRangeAddr addr) {
        return XDDFDataSourcesFactory.fromNumericCellRange(
                (XSSFSheet)sheet,
                new CellRangeAddress(
                        addr.getFirstRow(),
                        addr.getLastRow(),
                        addr.getFirstCol(),
                        addr.getLastCol()));
    }

    private XDDFDataSource<String> stringDataSource(final CellRangeAddr addr) {
        return XDDFDataSourcesFactory.fromStringCellRange(
                (XSSFSheet)sheet,
                new CellRangeAddress(
                        addr.getFirstRow(),
                        addr.getLastRow(),
                        addr.getFirstCol(),
                        addr.getLastCol()));
    }
    private LegendPosition toLegendPosition(final Position pos) {
        if (pos == null) {
            return LegendPosition.TOP;
        }
        else {
            switch(pos) {
                case BOTTOM:    return LegendPosition.BOTTOM;
                case LEFT:      return LegendPosition.LEFT;
                case RIGHT:     return LegendPosition.RIGHT;
                case TOP:       return LegendPosition.TOP;
                case TOP_RIGHT: return LegendPosition.TOP_RIGHT;
                default:        return LegendPosition.TOP;
            }
        }
    }

    private AxisPosition toAxisPosition(final Position pos) {
        if (pos == null) {
            return AxisPosition.TOP;
        }
        else {
            switch(pos) {
                case BOTTOM:    return AxisPosition.BOTTOM;
                case LEFT:      return AxisPosition.LEFT;
                case RIGHT:     return AxisPosition.RIGHT;
                case TOP:       return AxisPosition.TOP;
                case TOP_RIGHT: return AxisPosition.TOP;
                default:        return AxisPosition.TOP;
            }
        }
    }

    private final Sheet sheet;
}
