#pragma once

#include <QWidget>

/**
 * 自定义的带有箭头的弹出式窗口，可以自定义箭头方向
 */
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
    // 该信号用于失去焦点后自动关闭本窗口
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