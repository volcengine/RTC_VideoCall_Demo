#pragma once

#include <QWidget>

class QHBoxLayout;
class QSpacerItem;

namespace Ui {
class FocusVideoView;
}

class FocusVideoView : public QWidget {
  Q_OBJECT

 public:
  explicit FocusVideoView(QWidget *parent = nullptr);
  ~FocusVideoView();
  void init();
  void showWidget(int cnt);
  void wheelEvent(QWheelEvent *) override;

 protected:
  void paintEvent(QPaintEvent *) override;

 private:
  Ui::FocusVideoView* ui;
  int cnt_ = 0;
};
