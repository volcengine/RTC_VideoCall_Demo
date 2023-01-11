#pragma once

#include <QWidget>

class PopupArrowWidget : public QWidget
{
    Q_OBJECT
public:
    PopupArrowWidget(QWidget* parent = nullptr);
    ~PopupArrowWidget() = default;

    void addCustomWidget(QWidget* widget);
    void setPopupPosition();
    enum class ArrowPosition
    {
        left = 0,
        right,
        top,
        bottom
    };
    void setArrowPosition(ArrowPosition position);

signals:
    void widgetVisiblilityChanged(bool isVisibled);

protected:
    void paintEvent(QPaintEvent* event) override;
    void showEvent(QShowEvent* event) override;
    void hideEvent(QHideEvent* event) override;

private:
    ArrowPosition arrowPosition_{ ArrowPosition::left };
    int ArrowRectWidth{ 12 };
    int ArrowRectHeight{ 6 };
    QColor backgroundColor{ "#1F2127" };
};